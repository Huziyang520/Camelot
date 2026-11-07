package com.camelot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Camelot {

    /** 与元数据、资源路径和 mixin 配置绑定的固定 id，不得修改。 */
    public static final String MOD_ID = "camelot";

    public static final String MOD_NAME = "Camelot";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Camelot() {
    }
}
