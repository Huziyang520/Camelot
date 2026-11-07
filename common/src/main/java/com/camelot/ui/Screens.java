package com.camelot.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Single entry point for screen switches: queued on the client thread to avoid a forced extra frame.
 */
public final class Screens {

    private Screens() {
    }

    /** Opens (or closes with {@code null}) a screen. */
    public static void open(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> set(minecraft, screen));
    }

    private static void set(Minecraft minecraft, Screen screen) {
        try {
            Minecraft.class.getMethod("setScreen", Screen.class).invoke(minecraft, screen);
            return;
        } catch (ReflectiveOperationException ignored) {
            // 26.3 moved the screen holder to Gui
        }
        try {
            Object gui = Minecraft.class.getField("gui").get(minecraft);
            gui.getClass().getMethod("setScreen", Screen.class).invoke(gui, screen);
        } catch (ReflectiveOperationException e) {
            minecraft.setScreenAndShow(screen);
        }
    }
}
