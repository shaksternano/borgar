package io.github.shaksternano.borgar.media.io.reader;

import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.MediaUtil;
import io.github.shaksternano.borgar.media.VideoFrame;
import io.github.shaksternano.borgar.media.io.MediaReaderFactory;
import io.github.shaksternano.borgar.util.collection.ClosableIterator;
import io.github.shaksternano.borgar.util.collection.ClosableSpliterator;
import io.github.shaksternano.borgar.util.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrimageGifReader extends BaseMediaReader<ImageFrame> {

    private final List<ImageFrame> frames = new ArrayList<>();
    @Nullable
    private Reversed reversed;

    public ScrimageGifReader(File input, String format) throws IOException {
        this(ImageSource.of(input), format);
    }

    public ScrimageGifReader(InputStream input, String format) throws IOException {
        this(ImageSource.of(input), format);
        input.close();
    }

    private ScrimageGifReader(ImageSource imageSource, String format) throws IOException {
        super(format);
        var gif = AnimatedGifReader.read(imageSource);
        frameCount = gif.getFrameCount();
        if (frameCount <= 0) {
            throw new IOException("Could not read any frames!");
        }
        var totalDuration = 0;
        for (var i = 0; i < frameCount; i++) {
            var image = gif.getFrame(i).awt();
            var frameDuration = gif.getDelay(i).toMillis() * 1000;
            frames.add(new ImageFrame(image, frameDuration, totalDuration));
            totalDuration += frameDuration;
        }
        duration = totalDuration;
        frameDuration = (double) duration / frameCount;
        frameRate = 1_000_000 / frameDuration;
        var dimensions = gif.getDimensions();
        width = dimensions.width;
        height = dimensions.height;
    }

    @Override
    public ImageFrame readFrame(long timestamp) {
        return MediaUtil.frameAtTime(timestamp, frames, duration);
    }

    @Override
    public ImageFrame first() {
        return frames.get(0);
    }

    @Override
    public MediaReader<ImageFrame> reversed() {
        if (reversed == null) {
            reversed = new Reversed();
        }
        return reversed;
    }

    @Override
    public ClosableIterator<ImageFrame> iterator() {
        return ClosableIterator.wrap(frames.iterator());
    }

    @Override
    public void forEach(Consumer<? super ImageFrame> action) {
        frames.forEach(action);
    }

    @Override
    public ClosableSpliterator<ImageFrame> spliterator() {
        return ClosableSpliterator.wrap(frames.spliterator());
    }

    @Override
    public void close() {
        frames.clear();
        if (reversed != null) {
            reversed.reversedFrames.clear();
        }
    }

    public enum Factory implements MediaReaderFactory<ImageFrame> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrame> createReader(File media, String format) throws IOException {
            return new ScrimageGifReader(media, format);
        }

        @Override
        public MediaReader<ImageFrame> createReader(InputStream media, String format) throws IOException {
            return new ScrimageGifReader(media, format);
        }
    }

    private class Reversed extends BaseMediaReader<ImageFrame> {

        private final List<ImageFrame> reversedFrames;

        private Reversed() {
            super(ScrimageGifReader.this.format());
            frameCount = ScrimageGifReader.this.frameCount;
            duration = ScrimageGifReader.this.duration;
            frameRate = ScrimageGifReader.this.frameRate;
            frameDuration = ScrimageGifReader.this.frameDuration;
            width = ScrimageGifReader.this.width;
            height = ScrimageGifReader.this.height;
            reversedFrames = reverseFrames(frames, ImageFrame::new);
        }

        private static <E extends VideoFrame<T, E>, T> List<E> reverseFrames(List<E> frames, TriFunction<T, Double, Long, E> frameGenerator) {
            List<E> reversed = new ArrayList<>(frames.size());
            double timestamp = 0;
            for (var i = frames.size() - 1; i >= 0; i--) {
                var frame = frames.get(i);
                reversed.add(frameGenerator.apply(frame.content(), frame.duration(), (long) timestamp));
                timestamp += frame.duration();
            }
            return reversed;
        }

        @Override
        public ImageFrame readFrame(long timestamp) {
            return MediaUtil.frameAtTime(timestamp, reversedFrames, duration);
        }

        @Override
        public MediaReader<ImageFrame> reversed() {
            return ScrimageGifReader.this;
        }

        @Override
        public void close() {
            ScrimageGifReader.this.close();
        }

        @NotNull
        @Override
        public ClosableIterator<ImageFrame> iterator() {
            return ClosableIterator.wrap(reversedFrames.iterator());
        }
    }
}
