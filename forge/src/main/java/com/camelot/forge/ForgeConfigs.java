package com.camelot.forge;

import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigScope;
import com.camelot.config.TomlStore;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ForgeConfigs {

    private ForgeConfigs() {
    }

    public static void register(CamelotConfig... configs) {
        if (configs.length == 0) {
            return;
        }
        Path dir = FMLPaths.CONFIGDIR.get();
        String namespace = configs[0].namespace();
        Path legacy = dir.resolve(namespace + ".toml");
        boolean client = FMLEnvironment.dist == Dist.CLIENT;
        List<CamelotConfig> bound = new ArrayList<>();
        for (CamelotConfig config : configs) {
            if (config.scope() == ConfigScope.CLIENT && !client) {
                continue;
            }
            config.bind(new TomlStore(dir, config.schema(), legacy));
            config.load();
            CamelotConfigs.register(config);
            bound.add(config);
        }
        deleteLegacy(legacy);
        if (client) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory(
                            (minecraft, parent) -> ClothScreens.create(parent, bound)));
        }
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
