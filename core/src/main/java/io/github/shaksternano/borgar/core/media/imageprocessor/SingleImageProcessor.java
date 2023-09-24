package io.github.shaksternano.borgar.core.media.imageprocessor;

import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.util.Pair;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.UnaryOperator;

public interface SingleImageProcessor<T> extends ImageProcessor<T> {

    BufferedImage transformImage(ImageFrameOld frame, T constantData) throws IOException;

    /**
     * Calculates data that remains constant for all frames,
     * and so only needs to be calculated once.
     *
     * @param image The first frame of the media.
     * @return The data.
     * @throws IOException If an I/O error occurs.
     */
    T constantData(BufferedImage image) throws IOException;

    @Override
    default <V> SingleImageProcessor<Pair<T, V>> andThen(SingleImageProcessor<V> after) {
        return new SingleImageProcessor<>() {
            @Override
            public BufferedImage transformImage(ImageFrameOld frame, Pair<T, V> constantData) throws IOException {
                var firstTransformed = frame.transform(
                    SingleImageProcessor.this.transformImage(frame, constantData.first())
                );
                return after.transformImage(firstTransformed, constantData.second());
            }

            @Override
            public Pair<T, V> constantData(BufferedImage image) throws IOException {
                return new Pair<>(
                    SingleImageProcessor.this.constantData(image),
                    after.constantData(image)
                );
            }

            @Override
            public float speed() {
                return SingleImageProcessor.this.speed() * after.speed();
            }

            @Override
            public void close() throws IOException {
                SingleImageProcessor.this.close();
                after.close();
            }
        };
    }

    @Override
    default SingleImageProcessor<T> andThen(UnaryOperator<BufferedImage> after) {
        return new SingleImageProcessor<>() {

            @Override
            public BufferedImage transformImage(ImageFrameOld frame, T constantData) throws IOException {
                var firstTransformed = SingleImageProcessor.this.transformImage(frame, constantData);
                return after.apply(firstTransformed);
            }

            @Override
            public T constantData(BufferedImage image) throws IOException {
                return SingleImageProcessor.this.constantData(image);
            }

            @Override
            public float speed() {
                return SingleImageProcessor.this.speed();
            }
        };
    }
}
