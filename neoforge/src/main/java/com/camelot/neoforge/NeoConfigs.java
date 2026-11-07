package com.camelot.neoforge;

import com.camelot.config.CamelotConfig;
import com.camelot.config.CamelotConfigs;
import com.camelot.config.ConfigScope;
import com.camelot.config.TomlStore;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class NeoConfigs {

    private NeoConfigs() {
    }

    public static void register(CamelotConfig... configs) {
        if (configs.length == 0) {
            return;
        }
        Path dir = FMLPaths.CONFIGDIR.get();
        String namespace = configs[0].namespace();
        Path legacy = dir.resolve(namespace + ".toml");
        boolean client = isClient();
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
            ModContainer container = ModList.get().getModContainerById(namespace).orElse(null);
            if (container != null) {
                container.registerExtensionPoint(IConfigScreenFactory.class,
                        (minecraft, parent) -> ClothScreens.create(parent, bound));
            }
        }
    }

    private static boolean isClient() {
        try {
            Class.forName("net.minecraft.client.Minecraft");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
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
