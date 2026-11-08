package com.camelot.neoforge;

import com.camelot.Camelot;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNet implements NetSender {

    public static final CustomPacketPayload.Type<ConfigPacket> TYPE =
            new CustomPacketPayload.Type<>(new ResourceLocation(Camelot.MOD_ID, "config"));

    public record ConfigPacket(ConfigPayload payload) implements CustomPacketPayload {

        public static final StreamCodec<RegistryFriendlyByteBuf, ConfigPacket> CODEC = StreamCodec.of(
                (buf, packet) -> packet.payload().write(buf),
                buf -> new ConfigPacket(ConfigPayload.read(buf)));

        @Override
        public CustomPacketPayload.Type<ConfigPacket> type() {
            return TYPE;
        }
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playBidirectional(TYPE, ConfigPacket.CODEC, (packet, context) ->
                context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) {
                        CamelotNet.onServerPayload(packet.payload(), player);
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
        return player.connection.hasChannel(TYPE);
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        PacketDistributor.sendToPlayer(player, new ConfigPacket(payload));
    }
}
