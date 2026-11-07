package com.camelot.neoforge;

import com.camelot.ui.ConfigScreens;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

/**
 * Installs NeoForge's own config screen for a mod.
 *
 * <p>Kept apart from {@link NeoConfigs} so a dedicated server never loads the client
 * classes; reached only from the client branch of the registration.
 */
final class NeoConfigScreens {

    private NeoConfigScreens() {
    }

    static void install(String namespace, ModContainer container) {
        ConfigScreens.register(namespace, parent -> new ConfigurationScreen(container, parent));
        TickHook.register();
    }
}
