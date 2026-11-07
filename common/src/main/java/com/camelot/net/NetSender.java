package com.camelot.net;

import net.minecraft.server.level.ServerPlayer;

/** Loader-specific transport, installed by each loader module at init. */
public interface NetSender {

    /** Client → server. Silently ignored when the client is not in a world. */
    void toServer(ConfigPayload payload);

    /** Whether this player's client registered the payload (false for vanilla clients). */
    boolean canReceive(ServerPlayer player);

    void toPlayer(ServerPlayer player, ConfigPayload payload);
}
