package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public final class NeoForgeNet implements NetSender {

    public static final ResourceLocation CHANNEL = new ResourceLocation(Camelot.MOD_ID, "config");

    public record ConfigPacket(ConfigPayload payload) implements CustomPacketPayload {

        @Override
        public void write(FriendlyByteBuf buf) {
            payload.write(buf);
        }

        @Override
        public ResourceLocation id() {
            return CHANNEL;
        }
    }

    public static void registerPayloads(RegisterPayloadHandlerEvent event) {
        IPayloadRegistrar registrar = event.registrar("1");
        registrar.play(CHANNEL, buf -> new ConfigPacket(ConfigPayload.read(buf)), (packet, context) ->
                context.workHandler().execute(() -> {
                    if (context.flow() == PacketFlow.SERVERBOUND) {
                        context.player().ifPresent(player -> {
                            if (player instanceof ServerPlayer serverPlayer) {
                                CamelotNet.onServerPayload(packet.payload(), serverPlayer);
                            }
                        });
                    } else {
                        CamelotNet.onClientPayload(packet.payload());
                    }
                }));
    }

    @Override
    public void toServer(ConfigPayload payload) {

        NeoForgeClientNet.send(payload);
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return NetworkRegistry.getInstance().isConnected(player.connection, CHANNEL);
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        PacketDistributor.PLAYER.with(player).send(new ConfigPacket(payload));
    }
}
