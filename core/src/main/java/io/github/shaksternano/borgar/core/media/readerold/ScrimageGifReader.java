package io.github.shaksternano.borgar.core.media.readerold;

import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.collect.ClosableSpliteratorOld;
import io.github.shaksternano.borgar.core.function.TriFunction;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.MediaReaderFactory;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import io.github.shaksternano.borgar.core.media.VideoFrameOld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScrimageGifReader extends BaseMediaReader<ImageFrameOld> {

    private final List<ImageFrameOld> frames = new ArrayList<>();
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
        var totalDuration = 0L;
        for (var i = 0; i < frameCount; i++) {
            var image = gif.getFrame(i).awt();
            var frameDuration = gif.getDelay(i).toMillis() * 1000;
            frames.add(new ImageFrameOld(image, frameDuration, totalDuration));
            totalDuration += frameDuration;
        }
        duration = totalDuration;
        frameDuration = (double) duration / frameCount;
        frameRate = 1_000_000 / frameDuration;
        var dimensions = gif.getDimensions();
        width = dimensions.width;
        height = dimensions.height;
        loopCount = gif.getLoopCount();
    }

    @Override
    public ImageFrameOld readFrame(long timestamp) {
        return MediaUtil.frameAtTime(timestamp, frames, duration);
    }

    @Override
    public ImageFrameOld first() {
        return frames.get(0);
    }

    @Override
    public MediaReader<ImageFrameOld> reversed() {
        if (reversed == null) {
            reversed = new Reversed();
        }
        return reversed;
    }

    @Override
    public ClosableIteratorOld<ImageFrameOld> iterator() {
        return ClosableIteratorOld.wrap(frames.iterator());
    }

    @Override
    public void forEach(Consumer<? super ImageFrameOld> action) {
        frames.forEach(action);
    }

    @Override
    public ClosableSpliteratorOld<ImageFrameOld> spliterator() {
        return ClosableSpliteratorOld.wrap(frames.spliterator());
    }

    @Override
    public void close() {
        frames.clear();
        if (reversed != null) {
            reversed.reversedFrames.clear();
        }
    }

    public enum Factory implements MediaReaderFactory<ImageFrameOld> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrameOld> createReader(File media, String format) throws IOException {
            return new ScrimageGifReader(media, format);
        }

        @Override
        public MediaReader<ImageFrameOld> createReader(InputStream media, String format) throws IOException {
            return new ScrimageGifReader(media, format);
        }
    }

    private class Reversed extends BaseMediaReader<ImageFrameOld> {

        private final List<ImageFrameOld> reversedFrames;

        private Reversed() {
            super(ScrimageGifReader.this.format());
            frameCount = ScrimageGifReader.this.frameCount;
            duration = ScrimageGifReader.this.duration;
            frameRate = ScrimageGifReader.this.frameRate;
            frameDuration = ScrimageGifReader.this.frameDuration;
            width = ScrimageGifReader.this.width;
            height = ScrimageGifReader.this.height;
            loopCount = ScrimageGifReader.this.loopCount;
            reversedFrames = reverseFrames(frames, ImageFrameOld::new);
        }

        private static <E extends VideoFrameOld<T, E>, T> List<E> reverseFrames(List<E> frames, TriFunction<T, Double, Long, E> frameGenerator) {
            List<E> reversed = new ArrayList<>(frames.size());
            double timestamp = 0;
            for (var i = frames.size() - 1; i >= 0; i--) {
                var frame = frames.get(i);
                reversed.add(frameGenerator.apply(frame.getContent(), frame.getDuration(), (long) timestamp));
                timestamp += frame.getDuration();
            }
            return reversed;
        }

        @Override
        public ImageFrameOld readFrame(long timestamp) {
            return MediaUtil.frameAtTime(timestamp, reversedFrames, duration);
        }

        @Override
        public MediaReader<ImageFrameOld> reversed() {
            return ScrimageGifReader.this;
        }

        @Override
        public void close() {
            ScrimageGifReader.this.close();
        }

        @NotNull
        @Override
        public ClosableIteratorOld<ImageFrameOld> iterator() {
            return ClosableIteratorOld.wrap(reversedFrames.iterator());
        }
    }
}
