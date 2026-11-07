package com.camelot.neoforge;

import com.camelot.config.CamelotConfigs;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client tick hook: keeps locally-edited configs fresh in a multiplayer world.
 * <p>Only loaded from the client branch of the registration, so a dedicated server
 * never touches it.
 */
final class TickHook {

    private static boolean registered;

    private TickHook() {
    }

    static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> CamelotConfigs.tickClient());
    }
}
