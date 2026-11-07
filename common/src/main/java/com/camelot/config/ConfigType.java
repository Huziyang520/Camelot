package com.camelot.config;

/**
 * Kind of value an option holds.
 *
 * <p>Values are stored in TOML and also travel over the network as text, so every
 * kind must be able to encode and restore itself without extra context.
 */
public enum ConfigType {

    /** TOML boolean. */
    BOOLEAN,

    /** TOML integer 0/1, presented as a toggle (keeps older file layout readable). */
    SWITCH,

    /** TOML string limited to a fixed set of choices. */
    SELECT,

    /** TOML list stored as {@code [option] blacklist = [...]}. */
    STRING_LIST
}
