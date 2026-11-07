package com.camelot.neoforge;

import com.camelot.config.CamelotConfigs;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

final class TickHook {

    private static boolean registered;

    private TickHook() {
    }

    static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                CamelotConfigs.tickClient();
            }
        });
    }
}
