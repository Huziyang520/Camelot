package com.camelot.config;

import java.util.List;

/**
 * One configurable value: key, kind, default and how it is presented.
 *
 * <p>Instances are immutable after the schema is built; the fluent setters are meant
 * to be used inside a {@code List.of(...)} literal.
 */
public final class ConfigOption {

    private final String key;
    private final ConfigType type;
    private final Object defaultValue;
    private final List<String> choices;
    private final int min;
    private final int max;

    private boolean synced = true;
    private boolean inGui = true;
    private String comment;
    private String legacyKey;

    private ConfigOption(String key, ConfigType type, Object defaultValue, List<String> choices, int min, int max) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.choices = List.copyOf(choices);
        this.min = min;
        this.max = max;
    }

    public static ConfigOption bool(String key, boolean defaultValue) {
        return new ConfigOption(key, ConfigType.BOOLEAN, defaultValue, List.of(), 0, 0);
    }

    public static ConfigOption switchInt(String key, int defaultValue) {
        return new ConfigOption(key, ConfigType.SWITCH, defaultValue == 0 ? 0 : 1, List.of(), 0, 1);
    }

    public static ConfigOption select(String key, String defaultValue, String... choices) {
        return new ConfigOption(key, ConfigType.SELECT, defaultValue, List.of(choices), 0, 0);
    }

    public static ConfigOption intRange(String key, int defaultValue, int min, int max) {
        return new ConfigOption(key, ConfigType.INT, defaultValue, List.of(), min, max);
    }

    public static ConfigOption list(String key, List<String> defaultValue) {
        return new ConfigOption(key, ConfigType.STRING_LIST, List.copyOf(defaultValue), List.of(), 0, 0);
    }

    /** Value belongs to this side only and is never sent over the network. */
    public ConfigOption clientOnly() {
        this.synced = false;
        return this;
    }

    /** Kept in the file but not shown in the config screen. */
    public ConfigOption hidden() {
        this.inGui = false;
        return this;
    }

    /** Text written as a comment above the option's table (list options only). */
    public ConfigOption comment(String text) {
        this.comment = text;
        return this;
    }

    /** Key read when {@link #key()} is missing; used once for renames. */
    public ConfigOption legacy(String oldKey) {
        this.legacyKey = oldKey;
        return this;
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

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    public boolean synced() {
        return synced;
    }

    public boolean inGui() {
        return inGui;
    }

    public String comment() {
        return comment;
    }

    public String legacyKey() {
        return legacyKey;
    }
}
