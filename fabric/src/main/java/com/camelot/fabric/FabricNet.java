package com.camelot.fabric;

import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNet implements NetSender {

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
                context.server().execute(() -> CamelotNet.onServerPayload(payload, context.player())));
    }

    @Override
    public void toServer(ConfigPayload payload) {

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
}
