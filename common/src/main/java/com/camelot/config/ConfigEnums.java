package com.camelot.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举取值的平台覆盖表。
 *
 * <p>NeoForge 的配置界面用枚举是否为 {@code TranslatableEnum} 决定显示译文还是常量名；
 * 业务模组在 NeoForge 侧另外声明同值枚举（带译文）并在此登记，公共 schema 保持不依赖平台类型。
 */
public final class ConfigEnums {

    private static final Map<String, Class<? extends Enum>> OVERRIDES = new ConcurrentHashMap<>();

    private ConfigEnums() {
    }

    public static void register(String namespace, String key, Class<? extends Enum> type) {
        OVERRIDES.put(namespace + "." + key, type);
    }

    /** 平台声明的枚举，缺省回退到 schema 自带的那个。 */
    public static Class<? extends Enum> of(String namespace, String key, Class<? extends Enum> fallback) {
        return OVERRIDES.getOrDefault(namespace + "." + key, fallback);
    }
}
