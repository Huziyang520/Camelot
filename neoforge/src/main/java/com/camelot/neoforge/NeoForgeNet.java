package com.camelot.neoforge;

import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class NeoForgeNet implements NetSender {

    @Override
    public void toServer(ConfigPayload payload) {

        NeoForgeClientNet.send(payload);
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return NetworkRegistry.hasChannel(player.connection, ConfigPayload.TYPE.id());
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }
}
