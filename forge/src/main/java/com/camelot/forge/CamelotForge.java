package com.camelot.forge;

import com.camelot.Camelot;
import net.minecraftforge.fml.common.Mod;

@Mod(Camelot.MOD_ID)
public final class CamelotForge {

    public CamelotForge() {
        Camelot.LOG.info("Camelot initialized (Forge)");
    }
}
