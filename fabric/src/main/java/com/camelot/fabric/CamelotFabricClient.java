package com.camelot.fabric;

import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/** Client half of the Fabric transport; never loaded on a dedicated server. */
public final class CamelotFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
                context.client().execute(() -> CamelotNet.onClientPayload(payload)));
    }

    /** Sends an update to the server; ignored outside a world or when the server has no such channel. */
    public static void send(ConfigPayload payload) {
        if (Minecraft.getInstance().getConnection() != null && ClientPlayNetworking.canSend(ConfigPayload.TYPE)) {
            ClientPlayNetworking.send(payload);
        }
    }
}
