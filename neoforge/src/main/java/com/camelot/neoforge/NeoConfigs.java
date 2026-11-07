package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigEnums;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigScope;
import com.camelot.config.ConfigSchema;
import com.camelot.io.Toml;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 把配置接到 NeoForge 配置系统：文件、取值与自带配置界面都由 NeoForge 管理。 */
public final class NeoConfigs {

    private NeoConfigs() {
    }

    /** 在模组构造器里调用，登记该模组的全部配置。 */
    public static void register(CamelotConfig... configs) {
        if (configs.length == 0) {
            return;
        }
        String namespace = configs[0].namespace();
        ModContainer container = ModList.get().getModContainerById(namespace).orElse(null);
        if (container == null) {
            Camelot.LOG.error("No mod container for {}, configs not registered", namespace);
            return;
        }
        boolean client = isClient();
        List<String> fileNames = new ArrayList<>();
        for (CamelotConfig config : configs) {
            if (config.scope() == ConfigScope.CLIENT && !client) {
                continue;
            }
            ModConfig.Type type = type(config.scope());
            // 显式给文件名：FML 的默认名会随版本变化（common / local），两端统一 <modid>-<scope>.toml
            String fileName = config.schema().fileName();
            importLegacy(config, fileName);
            healLegacyValues(config);
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            Map<String, ModConfigSpec.ConfigValue<?>> entries = new LinkedHashMap<>();
            Map<String, Class<? extends Enum>> enums = new LinkedHashMap<>();
            defineAll(builder, config.schema(), entries, enums);
            ModConfigSpec spec = builder.build();
            config.bind(new NeoConfigStore(spec, entries, config.schema(), enums));
            CamelotConfigs.register(config);
            container.registerConfig(type, spec, fileName);
            fileNames.add(fileName);
        }
        deleteLegacy(namespace, fileNames);
        if (client) {
            NeoConfigScreens.install(namespace, container);
        }
    }

    /** FML 26.3 内把 COMMON/SERVER 改为 LOCAL/SYNCED，按名查找才能同时兼容两个小版本。 */
    private static ModConfig.Type type(ConfigScope scope) {
        return scope == ConfigScope.CLIENT ? constant("CLIENT") : constant("LOCAL", "COMMON");
    }

    private static ModConfig.Type constant(String... names) {
        for (String name : names) {
            try {
                return ModConfig.Type.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // 该版本没有这个常量，试下一个
            }
        }
        throw new IllegalStateException("Unsupported ModConfig.Type: " + String.join("/", names));
    }

    /** 26.x 已移除 Dist，客户端专属用类是否存在来判断。 */
    private static boolean isClient() {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void defineAll(ModConfigSpec.Builder builder, ConfigSchema schema,
                                  Map<String, ModConfigSpec.ConfigValue<?>> entries,
                                  Map<String, Class<? extends Enum>> enums) {
        for (ConfigOption option : schema.options()) {
            if (option.comment() != null) {
                builder.comment(option.comment());
            }
            builder.translation(schema.labelKey(option));
            Class<? extends Enum> enumType = ConfigEnums.of(schema.namespace(), option.key(), option.enumType());
            if (enumType != null) {
                enums.put(option.key(), enumType);
            }
            entries.put(option.key(), define(builder, option, enumType));
        }
    }

    @SuppressWarnings("unchecked")
    private static ModConfigSpec.ConfigValue<?> define(ModConfigSpec.Builder builder, ConfigOption option,
                                                       Class<? extends Enum> enumType) {
        switch (option.type()) {
            case BOOLEAN:
                return builder.define(option.key(), (Boolean) option.defaultValue());
            case SWITCH:
                // 整数只会渲染成输入框；布尔才是勾选框，库内仍按 0/1 处理
                return builder.define(option.key(), (Integer) option.defaultValue() != 0);
            case SELECT:
                return builder.defineEnum(option.key(),
                        Enum.valueOf(enumType, (String) option.defaultValue()));
            case STRING_LIST:
                // 四参重载才带元素供给器，界面里才会出现“+”与删除按钮
                return builder.defineList(option.key(), (List<String>) option.defaultValue(),
                        () -> "", element -> element instanceof String);
            default:
                throw new IllegalArgumentException("Unsupported option type: " + option.type());
        }
    }

    /** 旧单文件里的值搬到新文件；新文件已存在则不动。 */
    private static void importLegacy(CamelotConfig config, String fileName) {
        Path dir = FMLPaths.CONFIGDIR.get();
        Path file = dir.resolve(fileName);
        Path legacy = dir.resolve(config.schema().namespace() + ".toml");
        if (Files.exists(file) || !Files.exists(legacy)) {
            return;
        }
        try {
            Map<String, Object> parsed = Toml.parse(Files.readString(legacy));
            Map<String, Object> values = new LinkedHashMap<>();
            for (ConfigOption option : config.schema().options()) {
                Object raw = parsed.get(option.key());
                if (raw == null && option.legacyKey() != null) {
                    raw = parsed.get(option.legacyKey());
                }
                if (raw != null) {
                    config.set(option.key(), raw);
                }
                values.put(option.key(), value(config, option));
            }
            Files.createDirectories(dir);
            Files.writeString(file, Toml.write(config.schema(), values));
        } catch (Exception e) {
            Camelot.LOG.error("Failed to import {}", legacy, e);
        }
    }

    /** 旧版本把列表存成 {@code {blacklist = [...]}} 字符串，启动时修一次并回写文件。 */
    private static void healLegacyValues(CamelotConfig config) {
        Path file = FMLPaths.CONFIGDIR.get().resolve(config.schema().fileName());
        if (!Files.exists(file)) {
            return;
        }
        try {
            Map<String, Object> parsed = Toml.parse(Files.readString(file));
            boolean dirty = false;
            for (ConfigOption option : config.schema().options()) {
                Object raw = parsed.get(option.key());
                if (raw == null) {
                    continue;
                }
                config.set(option.key(), raw);
                if (!String.valueOf(value(config, option)).equals(String.valueOf(raw))) {
                    dirty = true;
                }
            }
            if (dirty) {
                Map<String, Object> clean = new LinkedHashMap<>();
                for (ConfigOption option : config.schema().options()) {
                    clean.put(option.key(), value(config, option));
                }
                Files.writeString(file, Toml.write(config.schema(), clean));
            }
        } catch (Exception e) {
            Camelot.LOG.error("Failed to repair {}", file, e);
        }
    }

    /** 新文件写好后删除旧文件，避免"改了旧文件不生效"。 */
    private static void deleteLegacy(String namespace, List<String> written) {
        Path dir = FMLPaths.CONFIGDIR.get();
        boolean imported = written.stream().anyMatch(name -> Files.exists(dir.resolve(name)));
        if (!imported) {
            return;
        }
        try {
            Files.deleteIfExists(dir.resolve(namespace + ".toml"));
        } catch (Exception ignored) {
            // 删不掉不影响新文件生效
        }
    }

    private static Object value(CamelotConfig config, ConfigOption option) {
        switch (option.type()) {
            case BOOLEAN:
                return config.getBool(option.key());
            case SWITCH:
                return config.getInt(option.key());
            case SELECT:
                return config.getString(option.key());
            case STRING_LIST:
                return new ArrayList<>(config.getList(option.key()));
            default:
                return option.defaultValue();
        }
    }
}
