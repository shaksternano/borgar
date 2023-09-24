package io.github.shaksternano.borgar.core.media.readerold;

import io.github.shaksternano.borgar.core.collect.ClosableIteratorOld;
import io.github.shaksternano.borgar.core.collect.MappedList;
import io.github.shaksternano.borgar.core.media.FrameInfo;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.MediaReaderFactoryOld;
import io.github.shaksternano.borgar.core.media.MediaUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class WebPImageReader extends BaseMediaReader<ImageFrameOld> {

    private final ImageInputStream input;
    private final ImageReader reader;
    private final List<FrameInfo> frameInfos = new ArrayList<>();
    @Nullable
    private Reversed reversed;

    public WebPImageReader(File input, String format) throws IOException {
        this(ImageIO.createImageInputStream(input), format);
    }

    public WebPImageReader(InputStream input, String format) throws IOException {
        this(ImageIO.createImageInputStream(input), format);
        input.close();
    }

    private WebPImageReader(ImageInputStream input, String format) throws IOException {
        super(format);
        this.input = input;
        var readers = ImageIO.getImageReaders(input);
        if (!readers.hasNext()) {
            throw new IllegalArgumentException("No WebP reader found");
        }
        reader = readers.next();

        Class<?> webPReaderClass;
        Class<?> animationFrameClass;
        try {
            webPReaderClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.WebPImageReader");
            animationFrameClass = Class.forName("com.twelvemonkeys.imageio.plugins.webp.AnimationFrame");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!webPReaderClass.isInstance(reader)) {
            throw new IllegalArgumentException("No WebP reader found");
        }
        reader.setInput(input);
        frameCount = reader.getNumImages(true);

        Field framesField;
        Field durationField;
        try {
            framesField = webPReaderClass.getDeclaredField("frames");
            durationField = animationFrameClass.getDeclaredField("duration");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        framesField.setAccessible(true);
        durationField.setAccessible(true);

        var totalDuration = 0;
        List<?> animationFrames;
        try {
            animationFrames = (List<?>) framesField.get(reader);
            for (var animationFrame : animationFrames) {
                var frameDuration = (int) durationField.get(animationFrame) * 1000;
                frameInfos.add(new FrameInfo(frameDuration, totalDuration));
                totalDuration += frameDuration;
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (frameInfos.isEmpty()) {
            frameInfos.add(new FrameInfo(1, 0));
        }

        duration = totalDuration;
        frameDuration = (double) duration / frameCount;
        frameRate = 1_000_000 / frameDuration;
        width = reader.getWidth(0);
        height = reader.getHeight(0);
    }

    @Override
    public ImageFrameOld readFrame(long timestamp) throws IOException {
        var circularTimestamp = timestamp % Math.max(duration, 1);
        var index = MediaUtil.findIndex(circularTimestamp, new MappedList<>(frameInfos, FrameInfo::timestamp));
        return new ImageFrameOld(
            read(index),
            frameInfos.get(index).duration(),
            circularTimestamp
        );
    }

    @Override
    public MediaReader<ImageFrameOld> reversed() throws IOException {
        if (reversed == null) {
            reversed = new Reversed();
        }
        return reversed;
    }

    @Override
    public ClosableIteratorOld<ImageFrameOld> iterator() {
        return new Iterator();
    }

    @Override
    public void close() throws IOException {
        reader.dispose();
        input.close();
    }

    private BufferedImage read(int index) throws IOException {
        var image = reader.read(index);
        // Remove alpha as sometimes frames are completely transparent.
        for (var x = 0; x < image.getWidth(); x++) {
            for (var y = 0; y < image.getHeight(); y++) {
                var rgb = image.getRGB(x, y);
                var alpha = (rgb >> 24) & 0xFF;
                if (alpha == 0 && rgb != 0 && rgb != 0xFFFFFF) {
                    var noAlpha = rgb | 0xFF000000;
                    image.setRGB(x, y, noAlpha);
                }
            }
        }
        return image;
    }

    public enum Factory implements MediaReaderFactoryOld<ImageFrameOld> {

        INSTANCE;

        @Override
        public MediaReader<ImageFrameOld> createReader(File media, String format) throws IOException {
            return new WebPImageReader(media, format);
        }

        @Override
        public MediaReader<ImageFrameOld> createReader(InputStream media, String format) throws IOException {
            return new WebPImageReader(media, format);
        }
    }

    private class Iterator implements ClosableIteratorOld<ImageFrameOld> {

        private int index = 0;
        private long timestamp = 0;

        @Override
        public boolean hasNext() {
            return index < frameCount;
        }

        @Override
        public ImageFrameOld next() {
            try {
                var duration = frameInfos.get(index).duration();
                var frame = new ImageFrameOld(
                    read(index),
                    duration,
                    timestamp
                );
                index++;
                timestamp += (long) duration;
                return frame;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() {
        }
    }

    private class Reversed extends BaseMediaReader<ImageFrameOld> {

        private final List<IndexedFrameInfo> reversedFrameInfo;

        private Reversed() {
            super(WebPImageReader.this.format());
            frameCount = WebPImageReader.this.frameCount;
            duration = WebPImageReader.this.duration;
            frameRate = WebPImageReader.this.frameRate;
            frameDuration = WebPImageReader.this.frameDuration;
            width = WebPImageReader.this.width;
            height = WebPImageReader.this.height;
            loopCount = WebPImageReader.this.loopCount;
            reversedFrameInfo = reverseFrameInfos(WebPImageReader.this.frameInfos);
        }

        private static List<IndexedFrameInfo> reverseFrameInfos(List<FrameInfo> frameInfos) {
            List<IndexedFrameInfo> reversed = new ArrayList<>(frameInfos.size());
            double timestamp = 0;
            for (var i = frameInfos.size() - 1; i >= 0; i--) {
                var frameInfo = frameInfos.get(i);
                reversed.add(new IndexedFrameInfo(frameInfo.duration(), (long) timestamp, i));
                timestamp += frameInfo.duration();
            }
            return reversed;
        }

        @Override
        public ImageFrameOld readFrame(long timestamp) throws IOException {
            var circularTimestamp = timestamp % Math.max(duration, 1);
            var index = MediaUtil.findIndex(circularTimestamp, new MappedList<>(reversedFrameInfo, IndexedFrameInfo::timestamp));
            var frameInfo = reversedFrameInfo.get(index);
            return new ImageFrameOld(
                read(frameInfo.index()),
                frameInfo.duration(),
                circularTimestamp
            );
        }

        @Override
        public MediaReader<ImageFrameOld> reversed() {
            return WebPImageReader.this;
        }

        @Override
        public ClosableIteratorOld<ImageFrameOld> iterator() {
            return new ReversedIterator();
        }

        @Override
        public void close() throws IOException {
            WebPImageReader.this.close();
        }

        private record IndexedFrameInfo(double duration, long timestamp, int index) {
        }

        private class ReversedIterator implements ClosableIteratorOld<ImageFrameOld> {

            private final java.util.Iterator<IndexedFrameInfo> iterator = reversedFrameInfo.iterator();

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ImageFrameOld next() {
                var frameInfo = iterator.next();
                try {
                    var image = read(frameInfo.index());
                    return new ImageFrameOld(
                        image,
                        frameInfo.duration(),
                        frameInfo.timestamp()
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void close() {
            }
        }
    }
}
