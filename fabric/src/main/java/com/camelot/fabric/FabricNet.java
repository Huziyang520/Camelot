package com.camelot.fabric;

import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Fabric transport for {@link ConfigPayload}. */
public final class FabricNet implements NetSender {

    /** Payload type and server receiver; safe on both sides. */
    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
                context.server().execute(() -> CamelotNet.onServerPayload(payload, context.player())));
    }

    @Override
    public void toServer(ConfigPayload payload) {
        // Reached from the client only; the client class is resolved on first use.
        CamelotFabricClient.send(payload);
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, ConfigPayload.TYPE);
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void toAll(MinecraftServer server, ConfigPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (canReceive(player)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}
