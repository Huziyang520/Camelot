package com.camelot.fabric;

import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public final class CamelotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
                context.client().execute(() -> CamelotNet.onClientPayload(payload)));
        ClientTickEvents.END_CLIENT_TICK.register(client -> CamelotConfigs.tickClient());
    }

    public static void send(ConfigPayload payload) {
        if (Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(ConfigPayload.TYPE)) {
            ClientPlayNetworking.send(payload);
        }
    }
}
