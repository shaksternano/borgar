package io.github.shaksternano.mediamanipulator.media.io.imageprocessor;

import io.github.shaksternano.mediamanipulator.util.Pair;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.function.UnaryOperator;

public interface ImageProcessor<T> extends Closeable {

    default float speed() {
        return 1;
    }

    default float absoluteSpeed() {
        return Math.abs(speed());
    }

    <V> ImageProcessor<Pair<T, V>> andThen(SingleImageProcessor<V> after);

    ImageProcessor<T> andThen(UnaryOperator<BufferedImage> after);

    @Override
    default void close() throws IOException {
    }
}
