package io.github.shaksternano.mediamanipulator.util;

public class SystemUtil {

    private static final int TO_MB = 1024 * 1024;

    public static String getCurrentMemoryUsageMessage() {
        return "Current memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / TO_MB + "/" + Runtime.getRuntime().maxMemory() / TO_MB + "MB";
    }
}
