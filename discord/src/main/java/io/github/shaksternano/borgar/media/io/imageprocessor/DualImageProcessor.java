package io.github.shaksternano.borgar.media.io.imageprocessor;

import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.util.Pair;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.UnaryOperator;

public interface DualImageProcessor<T> extends ImageProcessor<T> {

    BufferedImage transformImage(ImageFrame frame1, ImageFrame frame2, T constantData) throws IOException;

    /**
     * Calculates data that remains constant for all frames,
     * and so only needs to be calculated once.
     *
     * @param image1 The first frame of the first media.
     * @param image2 The first frame of the second media.
     * @return The data.
     * @throws IOException If an I/O error occurs.
     */
    T constantData(BufferedImage image1, BufferedImage image2) throws IOException;

    @Override
    default <V> DualImageProcessor<Pair<T, V>> andThen(SingleImageProcessor<V> after) {
        return new DualImageProcessor<>() {
            @Override
            public BufferedImage transformImage(ImageFrame frame1, ImageFrame frame2, Pair<T, V> constantData) throws IOException {
                var firstTransformed = frame1.transform(
                    DualImageProcessor.this.transformImage(frame1, frame2, constantData.first())
                );
                return after.transformImage(firstTransformed, constantData.second());
            }

            @Override
            public Pair<T, V> constantData(BufferedImage image1, BufferedImage image2) throws IOException {
                return new Pair<>(
                    DualImageProcessor.this.constantData(image1, image2),
                    after.constantData(image1)
                );
            }

            @Override
            public float speed() {
                return DualImageProcessor.this.speed() * after.speed();
            }

            @Override
            public void close() throws IOException {
                DualImageProcessor.this.close();
                after.close();
            }
        };
    }

    @Override
    default DualImageProcessor<T> andThen(UnaryOperator<BufferedImage> after) {
        return new DualImageProcessor<>() {
            @Override
            public BufferedImage transformImage(ImageFrame frame1, ImageFrame frame2, T constantData) throws IOException {
                var firstTransformed = DualImageProcessor.this.transformImage(frame1, frame2, constantData);
                return after.apply(firstTransformed);
            }

            @Override
            public T constantData(BufferedImage image1, BufferedImage image2) throws IOException {
                return DualImageProcessor.this.constantData(image1, image2);
            }

            @Override
            public float speed() {
                return DualImageProcessor.this.speed();
            }
        };
    }
}
