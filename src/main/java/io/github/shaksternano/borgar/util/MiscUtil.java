package io.github.shaksternano.borgar.util;

import com.google.common.io.Closer;
import io.github.shaksternano.borgar.Main;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MiscUtil {

    public static final int TO_MB = 1024 * 1024;

    public static String getCurrentMemoryUsageMessage() {
        return "Current memory usage: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / TO_MB + "/" + Runtime.getRuntime().maxMemory() / TO_MB + "MB";
    }

    public static Logger createLogger(String name) {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.AsyncLoggerContextSelector");
        return LoggerFactory.getLogger(name);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static void closeAll(Iterable<? extends AutoCloseable> closeables) throws IOException {
        try (var closer = Closer.create()) {
            for (@Nullable var closable : closeables) {
                closer.register(AutoCloseableClosable.wrap(closable));
            }
        }
    }

    public static void closeAll(AutoCloseable... closeables) throws IOException {
        closeAll(Arrays.asList(closeables));
    }

    public static <T> CompletableFuture<T> repeatTry(
        Supplier<CompletableFuture<T>> toAttempt,
        int maxAttempts,
        int secondsBetweenAttempts,
        BiConsumer<Integer, Throwable> onAttemptFailure
    ) {
        return repeatTry(
            toAttempt,
            maxAttempts,
            secondsBetweenAttempts,
            onAttemptFailure,
            0
        );
    }

    private static <T> CompletableFuture<T> repeatTry(
        Supplier<CompletableFuture<T>> toAttempt,
        int maxAttempts,
        int secondsBetweenAttempts,
        BiConsumer<Integer, Throwable> onAttemptFailure,
        int attempts
    ) {
        return toAttempt.get()
            .exceptionallyCompose(throwable -> {
                var newAttempts = attempts + 1;
                onAttemptFailure.accept(newAttempts, throwable);
                if (newAttempts >= maxAttempts) {
                    return CompletableFuture.failedFuture(throwable);
                }
                try {
                    TimeUnit.SECONDS.sleep(secondsBetweenAttempts);
                } catch (InterruptedException e) {
                    Main.getLogger().error("Interrupted while waiting", e);
                }
                return repeatTry(toAttempt, maxAttempts, secondsBetweenAttempts, onAttemptFailure, newAttempts);
            });
    }

    public static boolean nullOrBlank(@Nullable String string) {
        return string == null || string.isBlank();
    }
}
