package com.camelot.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class Screens {

    private Screens() {
    }

    public static void open(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> set(minecraft, screen));
    }

    private static void set(Minecraft minecraft, Screen screen) {
        try {
            Minecraft.class.getMethod("setScreen", Screen.class).invoke(minecraft, screen);
            return;
        } catch (ReflectiveOperationException ignored) {

        }
        try {
            Object gui = Minecraft.class.getField("gui").get(minecraft);
            gui.getClass().getMethod("setScreen", Screen.class).invoke(gui, screen);
        } catch (ReflectiveOperationException e) {
            minecraft.setScreenAndShow(screen);
        }
    }
}
