package com.camelot.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class Screens {

    private Screens() {
    }

    public static void open(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.setScreen(screen));
    }
}
