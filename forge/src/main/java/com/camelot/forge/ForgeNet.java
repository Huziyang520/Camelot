package com.camelot.forge;

import com.camelot.Camelot;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import com.camelot.net.NetSender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ForgeNet implements NetSender {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Camelot.MOD_ID, "config"), () -> VERSION, VERSION::equals, VERSION::equals);

    public static void registerCommon() {
        CHANNEL.registerMessage(0, ConfigPayload.class, (payload, buf) -> payload.write(buf),
                ConfigPayload::read, (payload, supplier) -> {
                    NetworkEvent.Context context = supplier.get();
                    boolean toServer = context.getDirection() == NetworkDirection.PLAY_TO_SERVER;
                    context.enqueueWork(() -> {
                        if (toServer) {
                            CamelotNet.onServerPayload(payload, context.getSender());
                        } else {
                            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CamelotForgeClient.receive(payload));
                        }
                    });
                    context.setPacketHandled(true);
                });
    }

    @Override
    public void toServer(ConfigPayload payload) {

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CamelotForgeClient.send(payload));
    }

    @Override
    public boolean canReceive(ServerPlayer player) {
        return true;
    }

    @Override
    public void toPlayer(ServerPlayer player, ConfigPayload payload) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
