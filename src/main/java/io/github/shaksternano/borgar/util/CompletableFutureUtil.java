package io.github.shaksternano.borgar.util;

import io.github.shaksternano.borgar.util.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class CompletableFutureUtil {

    public static <T, R> CompletableFuture<R> reduceSequentiallyAsync(
        Iterable<T> iterable,
        @Nullable R initialValue,
        TriFunction<T, R, Integer, CompletableFuture<R>> function
    ) {
        CompletableFuture<R> future = CompletableFuture.completedFuture(initialValue);
        var i = 0;
        for (var element : iterable) {
            var index = i;
            future = future.thenCompose(value -> function.apply(element, value, index));
            i++;
        }
        return future;
    }

    public static <T> CompletableFuture<Void> forEachSequentiallyAsync(
        Iterable<T> iterable,
        BiFunction<T, Integer, CompletableFuture<?>> function
    ) {
        return reduceSequentiallyAsync(
            iterable,
            null,
            (element, unused, index) -> function.apply(element, index).thenApply(unused2 -> null)
        );
    }
}
