package com.camelot.config;

import com.camelot.net.CamelotNet;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Registry of every config owned by the mods that bundle this library. */
public final class CamelotConfigs {

    private static final Map<String, List<CamelotConfig>> REGISTRY = new LinkedHashMap<>();

    private CamelotConfigs() {
    }

    public static void register(CamelotConfig config) {
        List<CamelotConfig> configs = REGISTRY.computeIfAbsent(config.namespace(), key -> new ArrayList<>());
        if (!configs.contains(config)) {
            configs.add(config);
        }
    }

    /** The config of {@code namespace} that {@code scope} selects. */
    public static CamelotConfig get(String namespace, ConfigScope scope) {
        for (CamelotConfig config : REGISTRY.getOrDefault(namespace, List.of())) {
            if (config.scope() == scope) {
                return config;
            }
        }
        return null;
    }

    public static Iterable<CamelotConfig> all() {
        List<CamelotConfig> flat = new ArrayList<>();
        REGISTRY.values().forEach(flat::addAll);
        return flat;
    }

    /** Client-side pass: only local configs, so server values are never overwritten. */
    public static void tickClient() {
        for (CamelotConfig config : all()) {
            if (config.scope() == ConfigScope.CLIENT) {
                config.reload();
            }
        }
    }

    /** Reload pass; changed server-side configs are re-broadcast. */
    public static void tick(MinecraftServer server) {
        boolean resend = false;
        for (CamelotConfig config : all()) {
            boolean changed = config.reload();
            if (changed && config.synced()) {
                resend = true;
            }
        }
        if (resend) {
            CamelotNet.onServerReload(server);
        }
    }
}
