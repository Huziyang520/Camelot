package com.camelot.config;

import com.camelot.Camelot;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema-driven TOML config of one mod.
 *
 * <p>Values are kept in memory and mirrored to {@code config/<file>}, which is the
 * server-side source of truth. Syncable options are also mirrored to clients over
 * {@link com.camelot.net.CamelotNet}, so a client always shows the server's values.
 */
public final class CamelotConfig {

    /** The list item key inside every list table. */
    private static final String LIST_ITEM = "blacklist";

    /** Reload checks happen per tick and per consumer, so they are throttled. */
    private static final long RELOAD_CHECK_MS = 1000L;

    private final ConfigSchema schema;
    private final Path path;
    private final Map<String, Object> values = new LinkedHashMap<>();
    private volatile long lastModified = -1L;
    private volatile long lastCheck;

    public CamelotConfig(ConfigSchema schema) {
        this.schema = schema;
        this.path = Paths.get("config", schema.fileName());
        resetToDefaults();
    }

    public ConfigSchema schema() {
        return schema;
    }

    public String namespace() {
        return schema.namespace();
    }

    /** Values on disk win; anything missing or unreadable falls back to the default. */
    public void load() {
        resetToDefaults();
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path) || Files.size(path) == 0) {
                save();
                return;
            }
            Config root = new TomlParser().parse(Files.readString(path));
            for (ConfigOption option : schema.options()) {
                Object raw = readRaw(root, option);
                if (raw != null) {
                    values.put(option.key(), coerce(raw, option));
                }
            }
            lastModified = Files.getLastModifiedTime(path).toMillis();
        } catch (Exception e) {
            Camelot.LOG.error("Failed to read config {}", path, e);
        }
    }

    public void save() {
        try {
            Files.createDirectories(path.getParent());
            Config root = Config.inMemory();
            // Scalars first: a TOML writer folds every later scalar into a preceding table.
            for (ConfigOption option : schema.options()) {
                if (option.type() != ConfigType.STRING_LIST) {
                    root.set(option.key(), values.get(option.key()));
                }
            }
            for (ConfigOption option : schema.options()) {
                if (option.type() != ConfigType.STRING_LIST) {
                    continue;
                }
                Config table = Config.inMemory();
                table.set(LIST_ITEM, new ArrayList<>(getList(option.key())));
                root.set(option.key(), table);
            }
            String text = new TomlWriter().writeToString(root);
            Files.writeString(path, withComments(text));
            lastModified = Files.getLastModifiedTime(path).toMillis();
        } catch (Exception e) {
            Camelot.LOG.error("Failed to write config {}", path, e);
        }
    }

    /** Re-reads the file when it changed on disk (server side only; throttled). */
    public boolean reloadIfChanged() {
        long now = System.currentTimeMillis();
        if (now - lastCheck < RELOAD_CHECK_MS) {
            return false;
        }
        lastCheck = now;
        try {
            if (!Files.exists(path)) {
                return false;
            }
            long modified = Files.getLastModifiedTime(path).toMillis();
            if (modified == lastModified) {
                return false;
            }
            load();
            return true;
        } catch (Exception e) {
            Camelot.LOG.error("Failed to reload config {}", path, e);
            return false;
        }
    }

    // ─── values ───

    public boolean getBool(String key) {
        Object value = value(key);
        if (value instanceof Boolean b) {
            return b;
        }
        return value instanceof Number n && n.intValue() != 0;
    }

    public int getInt(String key) {
        Object value = value(key);
        return value instanceof Number n ? n.intValue() : 0;
    }

    public String getString(String key) {
        Object value = value(key);
        return value instanceof String s ? s : "";
    }

    @SuppressWarnings("unchecked")
    public List<String> getList(String key) {
        Object value = value(key);
        return value instanceof List ? (List<String>) value : Collections.emptyList();
    }

    public void set(String key, Object raw) {
        ConfigOption option = schema.option(key);
        if (option == null) {
            Camelot.LOG.warn("Unknown config key {}", key);
            return;
        }
        values.put(key, coerce(raw, option));
    }

    /** Syncable options as text, ready to be sent to the other side. */
    public Map<String, String> snapshotSynced() {
        Map<String, String> out = new LinkedHashMap<>();
        for (ConfigOption option : schema.options()) {
            if (option.synced()) {
                out.put(option.key(), encode(values.get(option.key()), option.type()));
            }
        }
        return out;
    }

    /** Applies values received from the other side; unknown or local options are skipped. */
    public void applyRemote(Map<String, String> remote) {
        for (Map.Entry<String, String> entry : remote.entrySet()) {
            ConfigOption option = schema.option(entry.getKey());
            if (option == null || !option.synced()) {
                continue;
            }
            values.put(option.key(), coerce(decode(entry.getValue(), option.type()), option));
        }
    }

    // ─── internals ───

    private Object value(String key) {
        Object value = values.get(key);
        if (value != null) {
            return value;
        }
        ConfigOption option = schema.option(key);
        return option == null ? null : option.defaultValue();
    }

    @SuppressWarnings("unchecked")
    private void resetToDefaults() {
        values.clear();
        for (ConfigOption option : schema.options()) {
            Object defaultValue = option.defaultValue();
            values.put(option.key(), defaultValue instanceof List<?> list
                    ? new ArrayList<>((List<String>) list)
                    : defaultValue);
        }
    }

    private Object readRaw(Config root, ConfigOption option) {
        Object raw = root.get(option.key());
        if (raw == null && option.legacyKey() != null) {
            raw = root.get(option.legacyKey());
        }
        return raw;
    }

    private Object coerce(Object raw, ConfigOption option) {
        Object fallback = option.defaultValue();
        switch (option.type()) {
            case BOOLEAN:
                if (raw instanceof Boolean b) return b;
                if (raw instanceof Number n) return n.intValue() != 0;
                return fallback;
            case SWITCH:
                if (raw instanceof Boolean b) return b ? 1 : 0;
                if (raw instanceof Number n) return n.intValue() == 0 ? 0 : 1;
                return fallback;
            case SELECT:
                if (!(raw instanceof String s)) return fallback;
                return option.choices().contains(s) ? s : fallback;
            case STRING_LIST:
                if (raw instanceof Config table) {
                    return toStringList(table.get(LIST_ITEM));
                }
                return toStringList(raw);
            default:
                return fallback;
        }
    }

    private static List<String> toStringList(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                if (item != null) {
                    String text = item.toString().trim();
                    if (!text.isEmpty()) {
                        out.add(text);
                    }
                }
            }
        } else if (raw instanceof String text && !text.isBlank()) {
            out.add(text.trim());
        }
        return out;
    }

    private static String encode(Object value, ConfigType type) {
        if (value == null) {
            return "";
        }
        if (type == ConfigType.STRING_LIST) {
            @SuppressWarnings("unchecked")
            List<String> list = (List<String>) value;
            return String.join("\n", list);
        }
        return String.valueOf(value);
    }

    private static Object decode(String text, ConfigType type) {
        switch (type) {
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

    /** Prepends each option's comment line above its assignment or table header. */
    private String withComments(String toml) {
        Map<String, String> comments = new LinkedHashMap<>();
        for (ConfigOption option : schema.options()) {
            if (option.comment() != null) {
                comments.put(option.type() == ConfigType.STRING_LIST
                        ? "[" + option.key() + "]"
                        : option.key() + " = ", option.comment());
            }
        }
        if (comments.isEmpty()) {
            return toml;
        }
        StringBuilder out = new StringBuilder();
        for (String line : toml.split("\n", -1)) {
            for (Map.Entry<String, String> entry : comments.entrySet()) {
                if (line.startsWith(entry.getKey())) {
                    out.append("# ").append(entry.getValue()).append('\n');
                }
            }
            out.append(line).append('\n');
        }
        return out.substring(0, out.length() - 1);
    }
}
