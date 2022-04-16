package io.github.shaksternano.mediamanipulator.mediamanipulation;

import com.google.common.collect.ImmutableSet;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import io.github.shaksternano.mediamanipulator.util.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum AnimatedImageManipulator implements MediaManipulator {

    INSTANCE;

    @Override
    public File caption(File media, String caption) {
        List<ImageDelay> captionedFrames = readGifFrames(media);

        captionedFrames.parallelStream().forEach(
                imageDelay -> imageDelay.setImage(ImageUtil.captionImage(imageDelay.getImage(), caption, Fonts.getCaptionFont()))
        );

        File captionedImageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_captioned").getName());
        writeFramesToGif(captionedFrames, captionedImageFile);
        return captionedImageFile;
    }

    @Override
    public ImmutableSet<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "gif"
        );
    }

    private static List<ImageDelay> readGifFrames(File media) {
        List<ImageDelay> frames = new ArrayList<>();
        GifDecoder decoder = new GifDecoder();
        decoder.read(media.getPath());

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            BufferedImage frame = decoder.getFrame(i);

            int delay = decoder.getDelay(i);
            frames.add(new ImageDelay(frame, delay));
        }

        return frames;
    }

    private static void writeFramesToGif(List<ImageDelay> frames, File outputFile) {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();

        encoder.start(outputFile.getPath());
        encoder.setRepeat(0);

        for (ImageDelay frame : frames) {
            encoder.setDelay(frame.getDelay());
            encoder.addFrame(frame.getImage());
        }

        encoder.finish();
    }

    private static class ImageDelay {
        private BufferedImage image;
        private final int DELAY;

        public ImageDelay(BufferedImage image, int delay) {
            this.image = image;
            DELAY = delay;
        }

        public BufferedImage getImage() {
            return image;
        }

        public int getDelay() {
            return DELAY;
        }

        public void setImage(BufferedImage image) {
            this.image = image;
        }
    }
}
