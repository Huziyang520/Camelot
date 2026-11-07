package com.camelot.platform;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mod presence check usable from loader-agnostic code.
 *
 * <p>Resolved by reflection so it works no matter which mod initialises first: the caller
 * (a config screen entry point) may run before this library's own entry point.
 */
public final class ModPresence {

    private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();

    private ModPresence() {
    }

    public static boolean isLoaded(String modId) {
        return CACHE.computeIfAbsent(modId, ModPresence::lookup);
    }

    private static boolean lookup(String modId) {
        try {
            Object loader = Class.forName("net.fabricmc.loader.api.FabricLoader")
                    .getMethod("getInstance").invoke(null);
            Method check = loader.getClass().getMethod("isModLoaded", String.class);
            return (Boolean) check.invoke(loader, modId);
        } catch (Throwable ignored) {
            // not Fabric
        }
        try {
            Object modList = Class.forName("net.neoforged.fml.ModList")
                    .getMethod("get").invoke(null);
            Method check = modList.getClass().getMethod("isLoaded", String.class);
            return (Boolean) check.invoke(modList, modId);
        } catch (Throwable ignored) {
            // not NeoForge
        }
        return false;
    }
}
