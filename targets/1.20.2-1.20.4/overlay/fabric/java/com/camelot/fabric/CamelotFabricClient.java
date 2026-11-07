package com.camelot.fabric;

import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public final class CamelotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FabricNet.CHANNEL, (client, handler, buf, responseSender) -> {
            ConfigPayload payload = ConfigPayload.read(buf);
            client.execute(() -> CamelotNet.onClientPayload(payload));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> CamelotConfigs.tickClient());
    }

    public static void send(ConfigPayload payload) {
        if (Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(FabricNet.CHANNEL)) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            payload.write(buf);
            ClientPlayNetworking.send(FabricNet.CHANNEL, buf);
        }
    }
}
