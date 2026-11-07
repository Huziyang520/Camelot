package com.camelot.notice;

import com.camelot.config.CamelotConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Join notice, gated by a toggle from the player's own config. */
public final class JoinNotice {

    private JoinNotice() {
    }

    /**
     * /**
     * Shows {@code messageKey} to the player unless {@code toggleKey} is off.
     */
    public static void show(Player player, CamelotConfig config, String toggleKey, String messageKey) {
        if (player == null || !config.getBool(toggleKey)) {
            return;
        }
        player.sendSystemMessage(Component.translatable(messageKey));
    }
}
