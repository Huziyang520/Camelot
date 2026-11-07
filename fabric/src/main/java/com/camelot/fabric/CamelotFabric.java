package com.camelot.fabric;

import com.camelot.Camelot;
import net.fabricmc.api.ModInitializer;

public final class CamelotFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Camelot.LOG.info("Camelot initialized (Fabric)");
    }
}
