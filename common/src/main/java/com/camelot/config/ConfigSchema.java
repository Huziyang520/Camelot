package com.camelot.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * File identity, translation keys and the ordered option list of one config.
 * <p>Order is significant: it is both the on-disk order and the screen order, so
 * functional options come first and presentation options come last.
 */
public final class ConfigSchema {

    private final String namespace;
    private final ConfigScope scope;
    private final String titleKey;
    private final List<ConfigOption> options;

    public ConfigSchema(String namespace, ConfigScope scope, String titleKey, List<ConfigOption> options) {
        this.namespace = namespace;
        this.scope = scope;
        this.titleKey = titleKey;
        this.options = List.copyOf(options);

        Set<String> seen = new HashSet<>();
        for (ConfigOption option : options) {
            if (!seen.add(option.key())) {
                throw new IllegalArgumentException("Duplicate config key: " + option.key());
            }
        }
    }

    public String namespace() {
        return namespace;
    }

    public ConfigScope scope() {
        return scope;
    }

    /** File name, {@code <modid>-<scope>.toml}. */
    public String fileName() {
        return namespace + "-" + scope.suffix() + ".toml";
    }

    public String titleKey() {
        return titleKey;
    }

    public List<ConfigOption> options() {
        return options;
    }

    /** Options shown in the config screen, in display order. */
    public List<ConfigOption> guiOptions() {
        List<ConfigOption> shown = new ArrayList<>();
        for (ConfigOption option : options) {
            if (option.inGui()) {
                shown.add(option);
            }
        }
        return shown;
    }

    public ConfigOption option(String key) {
        for (ConfigOption option : options) {
            if (option.key().equals(key)) {
                return option;
            }
        }
        return null;
    }

    /** Label key of an option, e.g. {@code gui.onlytp.mode}. */
    public String labelKey(ConfigOption option) {
        return "gui." + namespace + "." + option.key();
    }

    /** Label key of one choice of a select option, e.g. {@code gui.onlytp.mode.both}. */
    public String valueLabelKey(ConfigOption option, String choice) {
        return labelKey(option) + "." + choice;
    }
}
