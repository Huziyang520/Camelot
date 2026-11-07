package com.camelot.config;

import java.util.Arrays;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    public static ConfigOption select(String key, Enum<?> defaultValue) {
        Class<? extends Enum> type = defaultValue.getDeclaringClass();
        List<String> names = Arrays.stream(type.getEnumConstants()).map(Enum::name).toList();
        return new ConfigOption(key, ConfigType.SELECT, defaultValue.name(), names, type, null, null, true);
    }

    public static ConfigOption list(String key, List<String> defaultValue) {
        return new ConfigOption(key, ConfigType.STRING_LIST, List.copyOf(defaultValue), List.of(), null, null, null, true);
    }

    public ConfigOption comment(String text) {
        return new ConfigOption(key, type, defaultValue, choices, enumType, text, legacyKey, inGui);
    }

    public ConfigOption legacy(String oldKey) {
        return new ConfigOption(key, type, defaultValue, choices, enumType, comment, oldKey, inGui);
    }

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
