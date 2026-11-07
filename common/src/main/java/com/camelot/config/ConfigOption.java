package com.camelot.config;

import java.util.Arrays;
import java.util.List;

/**
 * One schema entry: key, type, default value and how it is presented.
 * <p>Instances are immutable; the builder methods return a copy.
 */
public final class ConfigOption {

    private final String key;
    private final ConfigType type;
    private final Object defaultValue;
    private final List<String> choices;
    private final Class<? extends Enum> enumType;
    private final String comment;
    private final String legacyKey;
    private final boolean inGui;

    private ConfigOption(String key, ConfigType type, Object defaultValue, List<String> choices,
                         Class<? extends Enum> enumType, String comment, String legacyKey, boolean inGui) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.choices = List.copyOf(choices);
        this.enumType = enumType;
        this.comment = comment;
        this.legacyKey = legacyKey;
        this.inGui = inGui;
    }

    public static ConfigOption bool(String key, boolean defaultValue) {
        return new ConfigOption(key, ConfigType.BOOLEAN, defaultValue, List.of(), null, null, null, true);
    }

    public static ConfigOption switchInt(String key, int defaultValue) {
        return new ConfigOption(key, ConfigType.SWITCH, defaultValue == 0 ? 0 : 1, List.of(), null, null, null, true);
    }

    /**
     * /**
     * 有限取值。用枚举而不是字符串：NeoForge 的配置界面把枚举渲染成"点一下换一个值"的按钮，
     */
    @SuppressWarnings("unchecked")
    public static ConfigOption select(String key, Enum<?> defaultValue) {
        Class<? extends Enum> type = defaultValue.getDeclaringClass();
        List<String> names = Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
        return new ConfigOption(key, ConfigType.SELECT, defaultValue.name(), names, type, null, null, true);
    }

    public static ConfigOption list(String key, List<String> defaultValue) {
        return new ConfigOption(key, ConfigType.STRING_LIST, List.copyOf(defaultValue), List.of(), null, null, null, true);
    }

    /** Tooltip shown by the screen, written above the key in the file. */
    public ConfigOption comment(String text) {
        return new ConfigOption(key, type, defaultValue, choices, enumType, text, legacyKey, inGui);
    }

    /** Key used by an older file version; read once when the new key is absent. */
    public ConfigOption legacy(String oldKey) {
        return new ConfigOption(key, type, defaultValue, choices, enumType, comment, oldKey, inGui);
    }

    /** Kept out of the generated screen, file-only. */
    public ConfigOption fileOnly() {
        return new ConfigOption(key, type, defaultValue, choices, enumType, comment, legacyKey, false);
    }

    public String key() {
        return key;
    }

    public ConfigType type() {
        return type;
    }

    public Object defaultValue() {
        return defaultValue;
    }

    public List<String> choices() {
        return choices;
    }

    public Class<? extends Enum> enumType() {
        return enumType;
    }

    public String comment() {
        return comment;
    }

    public String legacyKey() {
        return legacyKey;
    }

    public boolean inGui() {
        return inGui;
    }
}
