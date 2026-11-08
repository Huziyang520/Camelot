package com.camelot.platform;

import net.minecraft.world.entity.player.Player;

public final class Perms {

    private Perms() {
    }

    public static boolean canEdit(Player player) {
        return player == null || player.hasPermissions(2);
    }
}
