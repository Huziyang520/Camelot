package com.camelot.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigScreens {

    @FunctionalInterface
    public interface Gate {
        /**
         * Returns the screen that replaces the entry, or {@code null} to let it open.
         */
        Screen closed(Screen parent);
    }

    @FunctionalInterface
    public interface Factory {
        Screen create(Screen parent);
    }

    private static final Map<String, Factory> FACTORIES = new ConcurrentHashMap<>();
    private static final Map<String, Gate> GATES = new ConcurrentHashMap<>();

    private ConfigScreens() {
    }

    /**
     * Registers the per-mod gate consulted by every loader's mod-list entry.
     */
    public static void registerGate(String namespace, Gate gate) {
        GATES.put(namespace, gate);
    }

    public static void register(String namespace, Factory factory) {
        FACTORIES.put(namespace, factory);
    }

    public static boolean available(String namespace) {
        return FACTORIES.containsKey(namespace);
    }

    public static Screen create(String namespace, Screen parent) {
        Gate gate = GATES.get(namespace);
        if (gate != null) {
            Screen closed = gate.closed(parent);
            if (closed != null) {
                return closed;
            }
        }
        Factory factory = FACTORIES.get(namespace);
        return factory == null ? null : factory.create(parent);
    }

    public static Screen entryClosed(Screen parent, Component title, Component message, Component done) {
        return new EntryClosedScreen(parent, title, message, done);
    }
}
