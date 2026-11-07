package com.camelot.fabric;

import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigScope;
import com.camelot.config.TomlStore;
import com.camelot.ui.ConfigScreens;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FabricConfigs {

    private FabricConfigs() {
    }

    public static void register(CamelotConfig... configs) {
        if (configs.length == 0) {
            return;
        }
        Path dir = FabricLoader.getInstance().getConfigDir();
        String namespace = configs[0].namespace();
        Path legacy = dir.resolve(namespace + ".toml");
        List<CamelotConfig> bound = new ArrayList<>();
        for (CamelotConfig config : configs) {
            config.bind(new TomlStore(dir, config.schema(), legacy));
            config.load();
            CamelotConfigs.register(config);
            bound.add(config);
        }
        deleteLegacy(legacy);
        ConfigScreens.register(namespace, parent -> ClothScreens.create(parent, bound));
    }

    private static void deleteLegacy(Path legacy) {
        try {
            if (Files.exists(legacy)) {
                Files.delete(legacy);
            }
        } catch (Exception ignored) {
        }
    }
}
