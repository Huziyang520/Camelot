package com.camelot.fabric;

import com.camelot.Camelot;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNet implements NetSender {

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

    public static void registerCommon() {
        PayloadTypeRegistry.playC2S().register(TYPE, ConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(TYPE, ConfigPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TYPE, (packet, context) ->
                context.server().execute(() -> CamelotNet.onServerPayload(packet.payload(), context.player())));
    }

    @Override
    public void toServer(ConfigPayload payload) {

        CamelotFabricClient.send(payload);
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return ServerPlayNetworking.canSend(player, TYPE);
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        ServerPlayNetworking.send(player, new ConfigPacket(payload));
    }
}
