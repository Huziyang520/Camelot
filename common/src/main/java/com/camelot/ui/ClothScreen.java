package com.camelot.ui;

import com.camelot.config.CamelotConfig;
import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;
import com.camelot.net.CamelotNet;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the config screen from a schema with Cloth Config API.
 *
 * <p>This is the only class in the library that references Cloth types; it is reached
 * exclusively through {@link ConfigScreens#cloth}, which checks that the mod is loaded.
 *
 * <p>Edited values are held locally until Save, so closing the screen without saving
 * leaves the config untouched.
 */
public final class ClothScreen {

    private ClothScreen() {
    }

    public static Screen create(Screen parent, CamelotConfig config) {
        ConfigSchema schema = config.schema();
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable(schema.titleKey()));
        // 世界内非 OP 只能查看：服务端会拒绝其更新，界面也不应允许改动
        if (!canEdit()) {
            builder.setEditable(false);
        }
        ConfigEntryBuilder entries = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable(schema.titleKey()));

        Map<String, Object> pending = new LinkedHashMap<>();
        for (ConfigOption option : schema.guiOptions()) {
            category.addEntry(build(entries, config, option, pending));
        }

        builder.setSavingRunnable(() -> {
            pending.forEach(config::set);
            config.save();
            CamelotNet.sendUpdate(config);
        });
        return builder.build();
    }

    /** 主菜单（没有本地玩家）或拥有管理权限时才可编辑。 */
    private static boolean canEdit() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player == null
                || mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static AbstractConfigListEntry build(ConfigEntryBuilder entries, CamelotConfig config,
                                                 ConfigOption option, Map<String, Object> pending) {
        Component label = Component.translatable(config.schema().labelKey(option));
        Object defaultValue = option.defaultValue();
        switch (option.type()) {
            case BOOLEAN:
                return entries.startBooleanToggle(label, config.getBool(option.key()))
                        .setDefaultValue((Boolean) defaultValue)
                        .setSaveConsumer(value -> pending.put(option.key(), value))
                        .build();
            case SWITCH:
                return entries.startBooleanToggle(label, config.getInt(option.key()) != 0)
                        .setDefaultValue((Integer) defaultValue != 0)
                        .setSaveConsumer(value -> pending.put(option.key(), value ? 1 : 0))
                        .build();
            case SELECT:
                return entries.startStringDropdownMenu(label, config.getString(option.key()),
                                choice -> Component.translatable(config.schema().valueLabelKey(option, choice)))
                        .setSelections(option.choices())
                        .setDefaultValue((String) defaultValue)
                        .setSaveConsumer(value -> pending.put(option.key(), value))
                        .build();
            case INT:
                return entries.startIntSlider(label, config.getInt(option.key()), option.min(), option.max())
                        .setDefaultValue((Integer) defaultValue)
                        .setSaveConsumer(value -> pending.put(option.key(), value))
                        .build();
            case STRING_LIST:
                List<String> current = new ArrayList<>(config.getList(option.key()));
                return entries.startStrList(label, current)
                        .setDefaultValue(() -> new ArrayList<>((List<String>) defaultValue))
                        .setExpanded(true)
                        .setSaveConsumer(value -> pending.put(option.key(), new ArrayList<>(value)))
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported option type: " + option.type());
        }
    }
}
