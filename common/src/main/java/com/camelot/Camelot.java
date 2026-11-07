package com.camelot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camelot 的常量入口。本库不单独发布，由业务模组以 jar-in-jar 方式内置。
 */
public final class Camelot {

    public static final String MOD_ID = "camelot";
    public static final String MOD_NAME = "Camelot";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    private Camelot() {
    }
}
