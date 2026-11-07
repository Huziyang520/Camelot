package com.camelot.neoforge;

import com.camelot.net.ConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Client half of the NeoForge transport; never loaded on a dedicated server. */
final class NeoForgeClientNet {

    private NeoForgeClientNet() {
    }

    /** Sends an update to the server; ignored outside a world or when the server has no such channel. */
    static void send(ConfigPayload payload) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            return;
        }
        if (!NetworkRegistry.hasChannel(listener.getConnection(), ConnectionProtocol.PLAY, ConfigPayload.TYPE.id())) {
            return;
        }
        ClientPacketDistributor.sendToServer(payload);
    }
}
