package com.camelot.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform overrides for enum-valued options.
 *
 * <p>NeoForge shows a translated name only when the enum implements {@code TranslatableEnum},
 * so mods register an equivalent enum here; the schema itself stays platform-free.
 */
public final class ConfigEnums {

    private static final Map<String, Class<? extends Enum>> OVERRIDES = new ConcurrentHashMap<>();

    private ConfigEnums() {
    }

    public static void register(String namespace, String key, Class<? extends Enum> type) {
        OVERRIDES.put(namespace + "." + key, type);
    }

    /** Platform enum for this key, falling back to the schema's own. */
    public static Class<? extends Enum> of(String namespace, String key, Class<? extends Enum> fallback) {
        return OVERRIDES.getOrDefault(namespace + "." + key, fallback);
    }
}
