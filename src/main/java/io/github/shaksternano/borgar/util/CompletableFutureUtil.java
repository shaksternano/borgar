package io.github.shaksternano.borgar.util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CompletableFutureUtil {

    public static <T> CompletableFuture<List<T>> all(Collection<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
            .thenApply(unused -> futures.stream()
                .map(CompletableFuture::join)
                .toList()
            );
    }
}
