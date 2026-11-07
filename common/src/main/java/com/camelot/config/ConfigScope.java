package com.camelot.config;

/**
 * Which file a group of options lives in.
 * <p>Names follow the loader convention {@code <modid>-<scope>.toml}; {@link #CLIENT}
 * options never leave the machine they were edited on.
 */
public enum ConfigScope {
    COMMON("common"),
    CLIENT("client");

    private final String suffix;

    ConfigScope(String suffix) {
        this.suffix = suffix;
    }

    public String suffix() {
        return suffix;
    }
}
