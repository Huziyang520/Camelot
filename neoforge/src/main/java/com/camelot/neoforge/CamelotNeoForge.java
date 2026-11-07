package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Camelot.MOD_ID)

public final class CamelotNeoForge {

    public CamelotNeoForge(IEventBus modBus) {
        CamelotNet.setSender(new NeoForgeNet());

        modBus.addListener(CamelotNeoForge::registerPayloads);
        NeoForge.EVENT_BUS.addListener(CamelotNeoForge::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(CamelotNeoForge::onServerTick);

        Camelot.LOG.info("Camelot loaded (NeoForge)");
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {

        PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(ConfigPayload.TYPE, ConfigPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CamelotNet.onServerPayload(payload, player);
                    }
                }),
                (payload, context) -> context.enqueueWork(() -> CamelotNet.onClientPayload(payload)));
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
