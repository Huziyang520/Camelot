package com.camelot.neoforge;

import com.camelot.net.ConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.neoforged.neoforge.network.PacketDistributor;

final class NeoForgeClientNet {

    private NeoForgeClientNet() {
    }

    static void send(ConfigPayload payload) {
        ClientPacketListener listener = Minecraft.getInstance().getConnection();
        if (listener == null) {
            return;
        }
        PacketDistributor.sendToServer(new NeoForgeNet.ConfigPacket(payload));
    }
}
