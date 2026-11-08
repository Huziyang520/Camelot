package com.camelot.ui;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigScreens {

    @FunctionalInterface
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

    public static Screen entryClosed(Screen parent, Component title, Component message, Component done) {
        return new EntryClosedScreen(parent, title, message, done);
    }
}
