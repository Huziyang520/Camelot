package com.camelot.notice;

import com.camelot.config.CamelotConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public final class JoinNotice {

    private JoinNotice() {
    }

    public static void show(Player player, CamelotConfig config, String toggleKey, String messageKey) {
        if (player == null || !config.getBool(toggleKey)) {
            return;
        }
        player.sendSystemMessage(Component.translatable(messageKey));
    }
}
