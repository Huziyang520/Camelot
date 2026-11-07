package com.camelot.neoforge;

import com.camelot.config.ConfigStore;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link ConfigStore} backed by a NeoForge config spec.
 *
 * <p>The spec owns the file, so the file format, the generated screen and the runtime
 * values are one and the same object; this class is only the bridge to the library's
 * key/value view.
 */
final class NeoConfigStore implements ConfigStore {

    private final ModConfigSpec spec;
    private final Map<String, ModConfigSpec.ConfigValue<?>> entries;

    NeoConfigStore(ModConfigSpec spec, Map<String, ModConfigSpec.ConfigValue<?>> entries) {
        this.spec = spec;
        this.entries = new LinkedHashMap<>(entries);
    }

    @Override
    public boolean read(Map<String, Object> values) {
        if (!spec.isLoaded()) {
            return false;
        }
        boolean changed = false;
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : entries.entrySet()) {
            Object stored = copy(entry.getValue().get());
            if (!Objects.equals(values.get(entry.getKey()), stored)) {
                values.put(entry.getKey(), stored);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void write(Map<String, Object> values) {
        if (!spec.isLoaded()) {
            return;
        }
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : entries.entrySet()) {
            Object value = values.get(entry.getKey());
            if (value != null) {
                ((ModConfigSpec.ConfigValue) entry.getValue()).set(copy(value));
            }
        }
    }

    private static Object copy(Object value) {
        return value instanceof List<?> list ? new ArrayList<>(list) : value;
    }
}
