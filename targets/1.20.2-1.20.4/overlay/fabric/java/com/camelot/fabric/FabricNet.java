package com.camelot.fabric;

import com.camelot.Camelot;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNet implements NetSender {

    public static final ResourceLocation CHANNEL = new ResourceLocation(Camelot.MOD_ID, "config");

    public static void registerCommon() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            ConfigPayload payload = ConfigPayload.read(buf);
            server.execute(() -> CamelotNet.onServerPayload(payload, player));
        });
    }

    @Override
    public void toServer(ConfigPayload payload) {

        CamelotFabricClient.send(payload);
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, CHANNEL);
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
