package com.camelot.neoforge;

import com.camelot.net.ConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

final class NeoForgeClientNet {

    private NeoForgeClientNet() {
    }

    static void send(ConfigPayload payload) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            return;
        }
        if (!NetworkRegistry.hasChannel(listener.getConnection(), ConnectionProtocol.PLAY, ConfigPayload.TYPE.id())) {
            return;
        }
        PacketDistributor.sendToServer(payload);
    }
}
