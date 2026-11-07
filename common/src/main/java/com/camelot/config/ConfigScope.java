package com.camelot.config;

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
