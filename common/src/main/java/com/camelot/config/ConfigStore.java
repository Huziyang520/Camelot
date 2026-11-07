package com.camelot.config;

import java.util.Map;

public interface ConfigStore {

    boolean read(Map<String, Object> values);

    void write(Map<String, Object> values);
}
