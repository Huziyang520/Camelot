package com.camelot.neoforge;

import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

final class PayloadRegistration {

    private PayloadRegistration() {
    }

    static void register(PayloadRegistrar registrar) {
        registrar.playBidirectional(ConfigPayload.TYPE, ConfigPayload.CODEC, PayloadRegistration::handle);
    }

    private static void handle(ConfigPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                CamelotNet.onServerPayload(payload, player);
            } else {
                CamelotNet.onClientPayload(payload);
            }
        });
    }
}
