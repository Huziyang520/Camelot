package com.camelot.fabric;

import com.camelot.config.CamelotConfig;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;
import com.camelot.net.CamelotNet;
import com.camelot.platform.ModPresence;
import com.camelot.platform.Perms;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class ClothScreens {

    private static final String CLOTH = "cloth-config";
    private static final String CLOTH_LEGACY = "cloth_config";

    private ClothScreens() {
    }

    static Screen create(Screen parent, List<CamelotConfig> configs) {
        if (!ModPresence.isLoaded(CLOTH) && !ModPresence.isLoaded(CLOTH_LEGACY)) {
            return null;
        }
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(configs.get(0).schema().titleKey()));
        if (!canEdit()) {
            builder.setEditable(false);
        }
        ConfigEntryBuilder entries = builder.entryBuilder();
        Map<CamelotConfig, Map<String, Object>> pending = new LinkedHashMap<>();
        for (CamelotConfig config : configs) {
            ConfigSchema schema = config.schema();
            ConfigCategory category = builder.getOrCreateCategory(
                    Component.translatable("gui." + schema.namespace() + ".scope." + schema.scope().suffix()));
            Map<String, Object> edited = new LinkedHashMap<>();
            pending.put(config, edited);
            for (ConfigOption option : schema.guiOptions()) {
                category.addEntry(build(entries, config, option, edited));
            }
        }
        builder.setSavingRunnable(() -> pending.forEach((config, edited) -> {
            edited.forEach(config::set);
            config.save();
            if (config.scope() == com.camelot.config.ConfigScope.COMMON) {
                CamelotNet.sendUpdate(config);
            }
        }));
        return builder.build();
    }

    private static boolean canEdit() {
        Minecraft mc = Minecraft.getInstance();
        return Perms.canEdit(mc.player);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static AbstractConfigListEntry build(ConfigEntryBuilder entries, CamelotConfig config,
                                                 ConfigOption option, Map<String, Object> edited) {
        Component label = Component.translatable(config.schema().labelKey(option));
        Object defaultValue = option.defaultValue();
        switch (option.type()) {
            case BOOLEAN:
                return entries.startBooleanToggle(label, config.getBool(option.key()))
                        .setDefaultValue((Boolean) defaultValue)
                        .setSaveConsumer(value -> edited.put(option.key(), value))
                        .build();
            case SWITCH:
                return entries.startBooleanToggle(label, config.getInt(option.key()) != 0)
                        .setDefaultValue((Integer) defaultValue != 0)
                        .setSaveConsumer(value -> edited.put(option.key(), value ? 1 : 0))
                        .build();
            case SELECT:

                Class<Enum> type = (Class<Enum>) option.enumType();
                Enum<?> current = Enum.valueOf(type, config.getString(option.key()));
                Enum<?> fallback = Enum.valueOf(type, (String) defaultValue);
                return entries.startEnumSelector(label, type, (Enum) current)
                        .setEnumNameProvider(value -> Component.translatable(
                                config.schema().valueLabelKey(option, ((Enum<?>) value).name())))
                        .setDefaultValue((Enum) fallback)
                        .setSaveConsumer(value -> edited.put(option.key(), ((Enum<?>) value).name()))
                        .build();
            case STRING_LIST:
                return entries.startStrList(label, new ArrayList<>(config.getList(option.key())))
                        .setDefaultValue(() -> new ArrayList<>((List<String>) defaultValue))
                        .setExpanded(true)
                        .setSaveConsumer(value -> edited.put(option.key(), new ArrayList<>(value)))
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported option type: " + option.type());
        }
    }
}
