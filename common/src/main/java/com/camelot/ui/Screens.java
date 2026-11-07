package com.camelot.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * 界面切换的统一入口：排到客户端任务队列再设，避免回调里强制多渲染一帧导致遮罩闪一下。
 */
public final class Screens {

    private Screens() {
    }

    /** 打开（或用 {@code null} 关闭）界面。 */
    public static void open(Screen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> set(minecraft, screen));
    }

    private static void set(Minecraft minecraft, Screen screen) {
        try {
            Minecraft.class.getMethod("setScreen", Screen.class).invoke(minecraft, screen);
            return;
        } catch (ReflectiveOperationException ignored) {
            // 26.3 起屏幕持有者搬到了 Gui
        }
        try {
            Object gui = Minecraft.class.getField("gui").get(minecraft);
            gui.getClass().getMethod("setScreen", Screen.class).invoke(gui, screen);
        } catch (ReflectiveOperationException e) {
            minecraft.setScreenAndShow(screen);
        }
    }
}
