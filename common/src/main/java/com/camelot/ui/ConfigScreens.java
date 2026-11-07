package com.camelot.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Screen dispatch, one provider per mod namespace.
 * <p>The platform decides what a mod's config screen is: NeoForge registers its own
 * {@code ConfigurationScreen}, Fabric a screen generated with Cloth Config API. A
 */
public final class ConfigScreens {

    @FunctionalInterface
    /** Builds the config screen of one mod; may return {@code null}. */
    public interface Factory {
        Screen create(Screen parent);
    }

    private static final Map<String, Factory> FACTORIES = new ConcurrentHashMap<>();

    private ConfigScreens() {
    }

    public static void register(String namespace, Factory factory) {
        FACTORIES.put(namespace, factory);
    }

    public static boolean available(String namespace) {
        return FACTORIES.containsKey(namespace);
    }

    public static Screen create(String namespace, Screen parent) {
        Factory factory = FACTORIES.get(namespace);
        return factory == null ? null : factory.create(parent);
    }

    /** Second-level screen shown when the server has closed the config entry. */
    public static Screen entryClosed(Screen parent, Component title, Component message, Component done) {
        return new EntryClosedScreen(parent, title, message, done);
    }
}
