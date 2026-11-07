package com.camelot.config;

import java.util.Map;

/**
 * Where a config's values are persisted.
 * <p>The library ships a TOML store for loaders without a config system; loaders that
 * have one implement this over their own spec so the native screen and the file stay
 */
public interface ConfigStore {

    /**
     * /**
     * Reads the stored values into {@code values}.
     */
    boolean read(Map<String, Object> values);

    /** Writes the values out. */
    void write(Map<String, Object> values);
}
