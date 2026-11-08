package com.camelot.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigEnums {

    private static final Map<String, Class<? extends Enum>> OVERRIDES = new ConcurrentHashMap<>();

    private ConfigEnums() {
    }

    public static void register(String namespace, String key, Class<? extends Enum> type) {
        OVERRIDES.put(namespace + "." + key, type);
    }

    public static Class<? extends Enum> of(String namespace, String key, Class<? extends Enum> fallback) {
        return OVERRIDES.getOrDefault(namespace + "." + key, fallback);
    }
}
