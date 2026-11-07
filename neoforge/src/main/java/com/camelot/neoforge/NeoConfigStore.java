package com.camelot.neoforge;

import com.camelot.config.ConfigOption;
import com.camelot.config.ConfigSchema;
import com.camelot.config.ConfigStore;
import com.camelot.config.ConfigType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** {@link ConfigStore} 的 NeoForge 实现：值存在 spec 里，本类只做类型与列表拷贝的转换。 */
final class NeoConfigStore implements ConfigStore {

    private final ModConfigSpec spec;
    private final Map<String, ModConfigSpec.ConfigValue<?>> entries;
    private final ConfigSchema schema;
    private final Map<String, Class<? extends Enum>> enums;

    NeoConfigStore(ModConfigSpec spec, Map<String, ModConfigSpec.ConfigValue<?>> entries,
                   ConfigSchema schema, Map<String, Class<? extends Enum>> enums) {
        this.spec = spec;
        this.entries = new LinkedHashMap<>(entries);
        this.schema = schema;
        this.enums = new LinkedHashMap<>(enums);
    }

    @Override
    public boolean read(Map<String, Object> values) {
        if (!spec.isLoaded()) {
            return false;
        }
        boolean changed = false;
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : entries.entrySet()) {
            Object stored = stored(entry.getKey(), entry.getValue().get());
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
            ConfigOption option = schema.option(entry.getKey());
            Object value = values.get(entry.getKey());
            if (option == null || value == null) {
                continue;
            }
            ((ModConfigSpec.ConfigValue) entry.getValue()).set(toSpecValue(entry.getKey(), option, value));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object toSpecValue(String key, ConfigOption option, Object value) {
        switch (option.type()) {
            case SWITCH:
                return value instanceof Number number ? number.intValue() != 0 : Boolean.parseBoolean(String.valueOf(value));
            case SELECT:
                Class<? extends Enum> type = enums.getOrDefault(key, option.enumType());
                return Enum.valueOf(type, String.valueOf(value));
            case STRING_LIST:
                return new ArrayList<>((List<String>) value);
            default:
                return value;
        }
    }

    private Object stored(String key, Object value) {
        ConfigOption option = schema.option(key);
        if (option != null && option.type() == ConfigType.SWITCH) {
            return Boolean.TRUE.equals(value) ? 1 : 0;
        }
        return value instanceof Enum<?> constant ? constant.name() : copy(value);
    }

    private static Object copy(Object value) {
        return value instanceof List<?> list ? new ArrayList<>(list) : value;
    }
}
