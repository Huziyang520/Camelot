package com.camelot.net;

import net.minecraft.server.level.ServerPlayer;

public interface NetSender {

    void toServer(ConfigPayload payload);

    boolean canReceive(ServerPlayer player);

    void toPlayer(ServerPlayer player, ConfigPayload payload);
}
