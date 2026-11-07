package com.camelot.forge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import com.camelot.net.ConfigPayload;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Camelot.MOD_ID, value = Dist.CLIENT)
public final class CamelotForgeClient {

    private CamelotForgeClient() {
    }

    static void send(ConfigPayload payload) {
        if (Minecraft.getInstance().getConnection() == null) {
            return;
        }
        ForgeNet.CHANNEL.sendToServer(payload);
    }

    static void receive(ConfigPayload payload) {
        CamelotNet.onClientPayload(payload);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CamelotConfigs.tickClient();
        }
    }
}
