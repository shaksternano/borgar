package io.github.shaksternano.mediamanipulator.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiscUtil {

    public static final int TO_MB = 1024 * 1024;

    public static String getCurrentMemoryUsageMessage() {
        return "Current memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / TO_MB + "/" + Runtime.getRuntime().maxMemory() / TO_MB + "MB";
    }

    public static Logger createLogger(String name) {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        return LoggerFactory.getLogger(name);
    }
}
