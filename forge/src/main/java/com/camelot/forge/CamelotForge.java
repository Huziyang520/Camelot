package com.camelot.forge;

import com.camelot.Camelot;
import com.camelot.config.CamelotConfigs;
import com.camelot.net.CamelotNet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Camelot.MOD_ID)
public final class CamelotForge {

    public CamelotForge() {
        CamelotNet.setSender(new ForgeNet());
        ForgeNet.registerCommon();

        MinecraftForge.EVENT_BUS.addListener(CamelotForge::onPlayerLogin);
        MinecraftForge.EVENT_BUS.addListener(CamelotForge::onServerTick);

        Camelot.LOG.info("Camelot loaded (Forge)");
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CamelotNet.syncTo(player);
        }
    }

    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            CamelotConfigs.tick(event.getServer());
        }
    }
}
