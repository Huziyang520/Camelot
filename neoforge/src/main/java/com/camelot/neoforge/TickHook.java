package com.camelot.neoforge;

import com.camelot.config.CamelotConfigs;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

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
