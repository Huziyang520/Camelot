package com.camelot.ui;

import com.camelot.config.CamelotConfig;
import com.camelot.platform.ModPresence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Config screen dispatch.
 *
 * <p>The screen is generated from the config schema with Cloth Config API. Cloth is a
 * soft dependency: when it is missing, {@link #cloth} returns {@code null} and the
 * caller decides what to show instead.
 */
public final class ConfigScreens {

    private static final String CLOTH = "cloth-config";

    private ConfigScreens() {
    }

    public static boolean clothAvailable() {
        return ModPresence.isLoaded(CLOTH);
    }

    /**
     * Builds the config screen, or returns {@code null} when Cloth Config is absent.
     *
     * <p>The Cloth-referencing class is only loaded inside the guarded branch, so a
     * player without Cloth never touches those types.
     */
    public static Screen cloth(Screen parent, CamelotConfig config) {
        return clothAvailable() ? ClothScreen.create(parent, config) : null;
    }

    /** Second-level screen shown when the server has closed the config entry. */
    public static Screen entryClosed(Screen parent, Component title, Component message, Component done) {
        return new EntryClosedScreen(parent, title, message, done);
    }
}
