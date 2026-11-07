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

/**
 * Binds configs to their TOML files and installs the Cloth screen for them.
 *
 * <p>The mod list calls the screen through {@link ConfigScreens}; when Cloth Config API
 * is missing nothing is installed and the button stays disabled.
 */
public final class FabricConfigs {

    private FabricConfigs() {
    }

    /** Registers one mod's configs; call from the mod initializer. */
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

    /** The old single-file layout is imported into the new per-scope files and dropped. */
    private static void deleteLegacy(Path legacy) {
        try {
            if (Files.exists(legacy)) {
                Files.delete(legacy);
            }
        } catch (Exception ignored) {
            // 删不掉就留着，读取时已不再使用
        }
    }
}
