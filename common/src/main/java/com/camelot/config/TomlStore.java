package com.camelot.config;

import com.camelot.Camelot;
import com.camelot.io.Toml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link ConfigStore} backed by a flat TOML file in the loader's config directory.
 * <p>Reads are throttled and skip untouched files, so the per-tick reload pass costs
 * one stat call per config.
 */
public final class TomlStore implements ConfigStore {

    private static final long CHECK_INTERVAL_MS = 1000L;

    private final ConfigSchema schema;
    private final Path file;
    private final Path legacyFile;
    private long lastModified = -1L;
    private long lastCheck;

    public TomlStore(Path configDir, ConfigSchema schema, Path legacyFile) {
        this.schema = schema;
        this.file = configDir.resolve(schema.fileName());
        this.legacyFile = legacyFile;
    }

    public Path file() {
        return file;
    }

    @Override
    public boolean read(Map<String, Object> values) {
        long now = System.currentTimeMillis();
        if (now - lastCheck < CHECK_INTERVAL_MS) {
            return false;
        }
        lastCheck = now;
        try {
            if (!Files.exists(file)) {
                if (legacyFile == null || !Files.exists(legacyFile)) {
                    write(values);
                    return true;
                }
                Files.createDirectories(file.getParent());
                Map<String, Object> imported = new LinkedHashMap<>(values);
                apply(Toml.parse(Files.readString(legacyFile)), imported);
                boolean changed = !imported.equals(values);
                values.putAll(imported);
                write(values);
                return changed;
            }
            long modified = Files.getLastModifiedTime(file).toMillis();
            if (modified == lastModified) {
                return false;
            }
            Map<String, Object> before = new LinkedHashMap<>(values);
            apply(Toml.parse(Files.readString(file)), values);
            lastModified = modified;
            return !before.equals(values);
        } catch (Exception e) {
            Camelot.LOG.error("Failed to read {}", file, e);
            return false;
        }
    }

    @Override
    public void write(Map<String, Object> values) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, Toml.write(schema, values));
            lastModified = Files.getLastModifiedTime(file).toMillis();
        } catch (Exception e) {
            Camelot.LOG.error("Failed to write {}", file, e);
        }
    }

    private void apply(Map<String, Object> parsed, Map<String, Object> values) {
        Set<String> carried = new LinkedHashSet<>();
        for (ConfigOption option : schema.options()) {
            Object raw = parsed.get(option.key());
            if (raw == null && option.legacyKey() != null) {
                raw = parsed.get(option.legacyKey());
            }
            if (raw != null) {
                values.put(option.key(), coerce(raw, option));
                carried.add(option.key());
            }
        }
        if (carried.isEmpty()) {
            return;
        }
        // 全部键缺失时才视为空文件；只有部分键缺失（如新版本新增项）保留读到的部分
        for (ConfigOption option : schema.options()) {
            if (!carried.contains(option.key())) {
                values.putIfAbsent(option.key(), defaultValue(option));
            }
        }
    }

    private static Object defaultValue(ConfigOption option) {
        Object value = option.defaultValue();
        return value instanceof List<?> list ? new ArrayList<>(list) : value;
    }

    private static Object coerce(Object raw, ConfigOption option) {
        Object fallback = defaultValue(option);
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
}
