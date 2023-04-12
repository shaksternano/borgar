package io.github.shaksternano.mediamanipulator.util;

import com.google.common.io.Closer;
import io.github.shaksternano.mediamanipulator.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
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
    public static void closeAll(Iterable<? extends Closeable> closeables) throws IOException {
        try (var closer = Closer.create()) {
            for (var closable : closeables) {
                closer.register(closable);
            }
        }
    }

    public static void repeatTry(
        Supplier<CompletableFuture<?>> toAttempt,
        int maxAttempts,
        int timeBetweenAttempts,
        BiConsumer<Integer, Throwable> onAttemptFailure,
        IntConsumer onAllAttemptsFailed
    ) {
        repeatTry(
            toAttempt,
            maxAttempts,
            timeBetweenAttempts,
            onAttemptFailure,
            0
        ).thenAccept(success -> {
            if (!success) {
                onAllAttemptsFailed.accept(maxAttempts);
            }
        });
    }

    private static CompletableFuture<Boolean> repeatTry(
        Supplier<CompletableFuture<?>> toAttempt,
        int maxAttempts,
        int timeBetweenAttempts,
        BiConsumer<Integer, Throwable> onAttemptFailure,
        int attempts
    ) {
        if (attempts >= maxAttempts) {
            return CompletableFuture.completedFuture(false);
        }
        return toAttempt.get()
            .thenApply(ignored -> true)
            .exceptionallyCompose(throwable -> {
                var newAttempts = attempts + 1;
                onAttemptFailure.accept(newAttempts, throwable);
                try {
                    TimeUnit.SECONDS.sleep(timeBetweenAttempts);
                } catch (InterruptedException e) {
                    Main.getLogger().error("Interrupted while waiting", e);
                }
                return repeatTry(toAttempt, maxAttempts, timeBetweenAttempts, onAttemptFailure, newAttempts);
            });
    }
}
