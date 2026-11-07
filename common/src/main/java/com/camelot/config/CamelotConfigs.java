package com.camelot.config;

import com.camelot.net.CamelotNet;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Registry of the configs owned by the mods that bundle this library. */
public final class CamelotConfigs {

    private static final Map<String, CamelotConfig> REGISTRY = new ConcurrentHashMap<>();

    private CamelotConfigs() {
    }

    public static void register(CamelotConfig config) {
        REGISTRY.put(config.namespace(), config);
    }

    public static CamelotConfig get(String namespace) {
        return REGISTRY.get(namespace);
    }

    public static Iterable<CamelotConfig> all() {
        return REGISTRY.values();
    }

    /** Hot-reload pass driven by the server tick; changed configs are re-broadcast. */
    public static void tick(MinecraftServer server) {
        boolean changed = false;
        for (CamelotConfig config : REGISTRY.values()) {
            changed |= config.reloadIfChanged();
        }
        if (changed) {
            CamelotNet.onServerReload(server);
        }
    }
}
