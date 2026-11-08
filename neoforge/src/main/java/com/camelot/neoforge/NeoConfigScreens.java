package com.camelot.neoforge;

import com.camelot.platform.Perms;
import com.camelot.ui.ConfigScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

final class NeoConfigScreens {

    private NeoConfigScreens() {
    }

    static void install(String namespace, ModContainer container) {
        ConfigScreens.register(namespace,
                parent -> new ConfigurationScreen(container, parent, NeoConfigScreens::section));
        TickHook.register();
    }

    private static Screen section(ConfigurationScreen screen, ModConfig.Type type, ModConfig config, Component title) {
        return new LockedSection(screen, type, config, title);
    }

    static boolean canEdit() {
        return Perms.canEdit(Minecraft.getInstance().player);
    }

    private static final class LockedSection extends ConfigurationScreen.ConfigurationSectionScreen {

        private final boolean locked = !canEdit();

        private LockedSection(Screen parent, ModConfig.Type type, ModConfig config, Component title) {
            super(parent, type, config, title, NeoConfigScreens::lockEntry);
        }

        @Override
        protected void setUndoButtonstate(boolean state) {
            super.setUndoButtonstate(!locked && state);
        }

        @Override
        protected void setResetButtonstate(boolean state) {
            super.setResetButtonstate(!locked && state);
        }
    }

    private static ConfigurationScreen.ConfigurationSectionScreen.Element lockEntry(
            ConfigurationScreen.ConfigurationSectionScreen.Context context, String key,
            ConfigurationScreen.ConfigurationSectionScreen.Element original) {
        if (original == null || canEdit()) {
            return original;
        }
        AbstractWidget widget = original.widget();
        if (widget != null) {
            widget.active = false;
            return original;
        }
        if (original.option() != null) {
            StringWidget frozen = new StringWidget(Button.DEFAULT_WIDTH, Button.DEFAULT_HEIGHT,
                    Component.literal(String.valueOf(original.option().get())), Minecraft.getInstance().font);
            frozen.active = false;
            return new ConfigurationScreen.ConfigurationSectionScreen.Element(
                    original.name(), original.tooltip(), frozen, original.undoable());
        }
        return original;
    }
}
