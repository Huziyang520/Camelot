package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigScope;
import com.camelot.config.ConfigSchema;
import com.camelot.config.ConfigType;
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

/**
 * 把配置接到 NeoForge 配置系统：每个 schema 生成一份 {@link ModConfigSpec}，
 * 文件、取值与自带配置界面都由 NeoForge 管理。
 */
public final class NeoConfigs {

    private NeoConfigs() {
    }

    /** Registers one mod's configs; call from the mod constructor. */
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
            // 显式给文件名：FML 的默认名会随版本变化（common / local），两端统一用 <modid>-<scope>.toml
            String fileName = config.schema().fileName();
            importLegacy(config, fileName);
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            Map<String, ModConfigSpec.ConfigValue<?>> entries = new LinkedHashMap<>();
            defineAll(builder, config.schema(), entries);
            ModConfigSpec spec = builder.build();
            config.bind(new NeoConfigStore(spec, entries, config.schema()));
            CamelotConfigs.register(config);
            container.registerConfig(type, spec, fileName);
            fileNames.add(fileName);
        }
        deleteLegacy(namespace, fileNames);
        if (client) {
            NeoConfigScreens.install(namespace, container);
        }
    }

    /**
     * /**
     * FML 在 26.3 内把 {@code COMMON}/{@code SERVER} 改名为 {@code LOCAL}/{@code SYNCED}，
     */
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
                                  Map<String, ModConfigSpec.ConfigValue<?>> entries) {
        for (ConfigOption option : schema.options()) {
            if (option.comment() != null) {
                builder.comment(option.comment());
            }
            builder.translation(schema.labelKey(option));
            entries.put(option.key(), define(builder, option));
        }
    }

    @SuppressWarnings("unchecked")
    private static ModConfigSpec.ConfigValue<?> define(ModConfigSpec.Builder builder, ConfigOption option) {
        switch (option.type()) {
            case BOOLEAN:
                return builder.define(option.key(), (Boolean) option.defaultValue());
            case SWITCH:
                return builder.define(option.key(), (Integer) option.defaultValue());
            case SELECT:
                return builder.defineEnum(option.key(),
                        Enum.valueOf(option.enumType(), (String) option.defaultValue()));
            case STRING_LIST:
                return builder.defineListAllowEmpty(option.key(), (List<String>) option.defaultValue(),
                        element -> element instanceof String);
            default:
                throw new IllegalArgumentException("Unsupported option type: " + option.type());
        }
    }

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

    private static void deleteLegacy(String namespace, List<String> written) {
        Path legacy = FMLPaths.CONFIGDIR.get().resolve(namespace + ".toml");
        boolean imported = written.stream().anyMatch(name -> Files.exists(FMLPaths.CONFIGDIR.get().resolve(name)));
        if (!imported) {
            return;
        }
        try {
            Files.deleteIfExists(legacy);
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
