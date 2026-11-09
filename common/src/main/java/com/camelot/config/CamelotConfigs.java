package com.camelot.config;

import com.camelot.net.CamelotNet;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CamelotConfigs {

    private static final Map<String, List<CamelotConfig>> REGISTRY = new LinkedHashMap<>();

    private CamelotConfigs() {
    }

    public static synchronized void register(CamelotConfig config) {
        List<CamelotConfig> configs = REGISTRY.computeIfAbsent(config.namespace(), key -> new ArrayList<>());
        if (!configs.contains(config)) {
            configs.add(config);
        }
    }

    public static synchronized CamelotConfig get(String namespace, ConfigScope scope) {
        for (CamelotConfig config : REGISTRY.getOrDefault(namespace, List.of())) {
            if (config.scope() == scope) {
                return config;
            }
        }
        return null;
    }

    public static synchronized Iterable<CamelotConfig> all() {
        List<CamelotConfig> flat = new ArrayList<>();
        REGISTRY.values().forEach(flat::addAll);
        return flat;
    }

    public static void tickClient() {
        for (CamelotConfig config : all()) {
            if (config.scope() == ConfigScope.CLIENT) {
                config.reload();
            }
        }
    }

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
