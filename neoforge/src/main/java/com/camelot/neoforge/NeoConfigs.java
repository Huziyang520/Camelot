package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
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

/**
 * Binds configs to the NeoForge config system.
 *
 * <p>Each schema becomes a {@link ModConfigSpec}, so the file, the values and the
 * generated {@code ConfigurationScreen} are NeoForge's own objects; the library only
 * keeps its key/value view on top.
 */
public final class NeoConfigs {

    private static final String LEGACY_SUFFIX = ".toml";

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
        for (CamelotConfig config : configs) {
            if (config.scope() == ConfigScope.CLIENT && !client) {
                continue;
            }
            importLegacy(config);
            ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
            Map<String, ModConfigSpec.ConfigValue<?>> entries = new LinkedHashMap<>();
            defineAll(builder, config.schema(), entries);
            ModConfigSpec spec = builder.build();
            config.bind(new NeoConfigStore(spec, entries));
            CamelotConfigs.register(config);
            container.registerConfig(type(config.scope()), spec);
        }
        deleteLegacy(namespace);
        if (client) {
            NeoConfigScreens.install(namespace, container);
        }
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

    private static ModConfig.Type type(ConfigScope scope) {
        return scope == ConfigScope.CLIENT ? ModConfig.Type.CLIENT : ModConfig.Type.COMMON;
    }

    private static void defineAll(ModConfigSpec.Builder builder, ConfigSchema schema,
                                  Map<String, ModConfigSpec.ConfigValue<?>> entries) {
        for (ConfigOption option : schema.options()) {
            String translation = schema.labelKey(option);
            if (option.comment() != null) {
                builder.comment(option.comment());
            }
            builder.translation(translation);
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
                return builder.defineInList(option.key(), (String) option.defaultValue(), option.choices());
            case STRING_LIST:
                return builder.defineListAllowEmpty(option.key(), (List<String>) option.defaultValue(),
                        element -> element instanceof String);
            default:
                throw new IllegalArgumentException("Unsupported option type: " + option.type());
        }
    }

    /** The old single-file layout is copied into the new per-scope file once. */
    private static void importLegacy(CamelotConfig config) {
        Path dir = FMLPaths.CONFIGDIR.get();
        Path file = dir.resolve(config.schema().fileName());
        Path legacy = dir.resolve(config.schema().namespace() + LEGACY_SUFFIX);
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
                values.put(option.key(), snapshotOf(config, option));
            }
            Files.createDirectories(dir);
            Files.writeString(file, Toml.write(config.schema(), values));
        } catch (Exception e) {
            Camelot.LOG.error("Failed to import {}", legacy, e);
        }
    }

    /** 旧单文件在导入后删除，避免与新文件并存造成"改了不生效"的误判。 */
    private static void deleteLegacy(String namespace) {
        try {
            Files.deleteIfExists(FMLPaths.CONFIGDIR.get().resolve(namespace + LEGACY_SUFFIX));
        } catch (Exception ignored) {
            // 删除失败不影响新文件生效
        }
    }

    private static Object snapshotOf(CamelotConfig config, ConfigOption option) {
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
