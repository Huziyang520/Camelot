package com.camelot.config;

import com.camelot.Camelot;
import com.camelot.io.Toml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CamelotConfig {

    private final ConfigSchema schema;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private ConfigStore store;

    public CamelotConfig(ConfigSchema schema) {
        this.schema = schema;
        resetToDefaults();
    }

    public void bind(ConfigStore configStore) {
        this.store = configStore;
    }

    public ConfigSchema schema() {
        return schema;
    }

    public String namespace() {
        return schema.namespace();
    }

    public ConfigScope scope() {
        return schema.scope();
    }

    public boolean synced() {
        return schema.scope() == ConfigScope.COMMON;
    }

    public void load() {
        if (store == null) {
            Camelot.LOG.warn("Config {} has no store bound", schema.fileName());
            return;
        }
        store.read(values);
    }

    public boolean reload() {
        if (store == null) {
            return false;
        }
        return store.read(values);
    }

    public void save() {
        if (store == null) {
            Camelot.LOG.warn("Config {} has no store bound", schema.fileName());
            return;
        }
        store.write(values);
    }

    public boolean getBool(String key) {
        Object value = value(key);
        if (value instanceof Boolean flag) {
            return flag;
        }
        return value instanceof Number number && number.intValue() != 0;
    }

    public int getInt(String key) {
        Object value = value(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    public String getString(String key) {
        Object value = value(key);
        return value instanceof String text ? text : "";
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        Object value = value(key);
        return value instanceof List<?> list ? (List<String>) list : Collections.emptyList();
    }

    public void set(String key, Object raw) {
        ConfigOption option = schema.option(key);
        if (option == null) {
            Camelot.LOG.warn("Unknown config key {}", key);
            return;
        }
        values.put(key, coerce(raw, option));
    }

    public Map<String, String> snapshot() {
        Map<String, String> out = new LinkedHashMap<>();
        for (ConfigOption option : schema.options()) {
            out.put(option.key(), encode(values.get(option.key())));
        }
        return out;
    }

    public void applyRemote(Map<String, String> remote) {
        for (Map.Entry<String, String> entry : remote.entrySet()) {
            ConfigOption option = schema.option(entry.getKey());
            if (option != null) {
                values.put(option.key(), coerce(decode(entry.getValue(), option), option));
            }
        }
    }

    private Object value(String key) {
        Object value = values.get(key);
        if (value != null) {
            return value;
        }
        ConfigOption option = schema.option(key);
        return option == null ? null : option.defaultValue();
    }

    private void resetToDefaults() {
        values.clear();
        for (ConfigOption option : schema.options()) {
            Object defaultValue = option.defaultValue();
            values.put(option.key(), defaultValue instanceof List<?> list
                    ? new ArrayList<>(list)
                    : defaultValue);
        }
    }

    private static Object coerce(Object raw, ConfigOption option) {
        Object fallback = option.defaultValue();
        switch (option.type()) {
            case BOOLEAN:
                if (raw instanceof Boolean flag) return flag;
                if (raw instanceof Number number) return number.intValue() != 0;
                return fallback;
            case SWITCH:
                if (raw instanceof Boolean flag) return flag ? 1 : 0;
                if (raw instanceof Number number) return number.intValue() == 0 ? 0 : 1;
                return fallback;
            case SELECT:
                return raw instanceof String text && option.choices().contains(text) ? text : fallback;
            case STRING_LIST:
                return raw instanceof List<?> list ? repair(list) : new ArrayList<String>();
            default:
                return fallback;
        }
    }

    private static List<String> repair(List<?> list) {
        List<String> out = new ArrayList<>();
        for (Object item : list) {
            if (item == null) {
                continue;
            }
            String text = item.toString().trim();
            if (text.isEmpty()) {
                continue;
            }
            if (text.startsWith("{") && text.contains("blacklist")) {
                out.addAll(Toml.unnest(text));
            } else {
                out.add(text);
            }
        }
        return out;
    }

    private static String encode(Object value) {
        if (value instanceof List<?> list) {
            return String.join("\n", list.stream().map(String::valueOf).toList());
        }
        return value == null ? "" : String.valueOf(value);
    }

    private static Object decode(String text, ConfigOption option) {
        switch (option.type()) {
            case BOOLEAN:
                return Boolean.parseBoolean(text);
            case SWITCH:
                return "1".equals(text) || "true".equalsIgnoreCase(text) ? 1 : 0;
            case STRING_LIST:
                return text.isEmpty() ? new ArrayList<String>() : new ArrayList<>(List.of(text.split("\n")));
            default:
                return text;
        }
    }
}
