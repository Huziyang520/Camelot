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

/**
 * {@link ConfigStore} 的 NeoForge 实现：值存在配置文件对应的 spec 里，
 * 本类只负责"枚举 ↔ 字符串"和列表拷贝这类转换。
 */
final class NeoConfigStore implements ConfigStore {

    private final ModConfigSpec spec;
    private final Map<String, ModConfigSpec.ConfigValue<?>> entries;
    private final ConfigSchema schema;

    NeoConfigStore(ModConfigSpec spec, Map<String, ModConfigSpec.ConfigValue<?>> entries, ConfigSchema schema) {
        this.spec = spec;
        this.entries = new LinkedHashMap<>(entries);
        this.schema = schema;
    }

    @Override
    public boolean read(Map<String, Object> values) {
        if (!spec.isLoaded()) {
            return false;
        }
        boolean changed = false;
        for (Map.Entry<String, ModConfigSpec.ConfigValue<?>> entry : entries.entrySet()) {
            Object stored = stored(entry.getValue().get());
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
            Object target = option.type() == ConfigType.SELECT && option.enumType() != null
                    ? Enum.valueOf(option.enumType(), String.valueOf(value))
                    : copy(value);
            ((ModConfigSpec.ConfigValue) entry.getValue()).set(target);
        }
    }

    private Object stored(Object value) {
        return value instanceof Enum<?> constant ? constant.name() : copy(value);
    }

    private static Object copy(Object value) {
        return value instanceof List<?> list ? new ArrayList<>(list) : value;
    }
}
