package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(Camelot.MOD_ID)

public final class CamelotNeoForge {

    public CamelotNeoForge(IEventBus modBus) {
        CamelotNet.setSender(new NeoForgeNet());

        modBus.addListener(NeoForgeNet::registerPayloads);
        NeoForge.EVENT_BUS.addListener(CamelotNeoForge::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(CamelotNeoForge::onServerTick);
        if (FMLEnvironment.dist.isClient()) {
            TickHook.register();
        }

        Camelot.LOG.info("Camelot loaded (NeoForge)");
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CamelotNet.syncTo(player);
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        CamelotConfigs.tick(event.getServer());
    }
}
