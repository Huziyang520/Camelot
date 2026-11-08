package com.camelot.fabric;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class CamelotFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CamelotNet.setSender(new FabricNet());
        FabricNet.registerCommon();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> CamelotNet.syncTo(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(CamelotConfigs::tick);

        Camelot.LOG.info("Camelot loaded (Fabric)");
    }
}
