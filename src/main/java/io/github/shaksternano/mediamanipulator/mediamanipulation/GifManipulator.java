package io.github.shaksternano.mediamanipulator.mediamanipulation;

import com.google.common.collect.ImmutableSet;
import com.madgag.gif.fmsware.AnimatedGifEncoder;
import com.madgag.gif.fmsware.GifDecoder;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public enum GifManipulator implements MediaManipulator {

    INSTANCE;

    /**
     * 8MB
     */
    private static final long TARGET_FILE_SIZE = 8388608;

    @Override
    public File caption(File media, String caption) {
        return applyToEachFrame(media, image -> ImageUtil.captionImage(image, caption, Fonts.getCaptionFont()), "captioned");
    }

    @Override
    public File stretch(File media, float widthMultiplier, float heightMultiplier) {
        return applyToEachFrame(media, image -> ImageUtil.stretch(image, widthMultiplier, heightMultiplier), "stretched");
    }

    @Override
    public File makeGif(File media) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "gif"
        );
    }

    private static File applyToEachFrame(File media, Function<BufferedImage, BufferedImage> operation, String operationName) {
        List<DelayedImage> frames = readGifFrames(media);
        frames = ImageUtil.removeFrames(frames, media.length(), TARGET_FILE_SIZE);

        frames.parallelStream().forEach(
                delayedImage -> {
                    BufferedImage uneditedImage = delayedImage.getImage();
                    BufferedImage image = operation.apply(uneditedImage);
                    delayedImage.setImage(image);
                    uneditedImage.flush();
                }
        );

        File imageFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());
        writeFramesToGifFile(frames, imageFile);
        return imageFile;
    }

    private static List<DelayedImage> readGifFrames(File media) {
        List<DelayedImage> frames = new ArrayList<>();
        GifDecoder decoder = new GifDecoder();
        decoder.read(media.getPath());

        for (int i = 0; i < decoder.getFrameCount(); i++) {
            BufferedImage frame = decoder.getFrame(i);
            int delay = decoder.getDelay(i);
            frames.add(new DelayedImage(frame, delay));
        }

        return frames;
    }

    private static List<DelayedImage> readGifFramesFallback(File media) throws IOException {
        List<DelayedImage> frames = new ArrayList<>();
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(media));

        for (int i = 0; i < gif.getFrameCount(); i++) {
            BufferedImage frame = gif.getFrame(i).awt();
            int delay = (int) gif.getDelay(i).toMillis();
            frames.add(new DelayedImage(frame, delay));
        }

        return frames;
    }

    private static void writeFramesToGifFileOther(List<DelayedImage> frames, File outputFile) {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();

        encoder.start(outputFile.getPath());
        encoder.setRepeat(0);

        Color transparent = new Color(0, 0, 0, 0);
        //encoder.setTransparent(Color.BLACK);
        //encoder.setBackground(Color.BLACK);
        //encoder.setDispose(0);

        for (DelayedImage frame : frames) {
            encoder.setDelay(frame.getDelay());
            encoder.addFrame(frame.getImage());
        }

        encoder.finish();
    }

    private static void writeFramesToGifFile(List<DelayedImage> frames, File outputFile){
        StreamingGifWriter writer = new StreamingGifWriter();
        try (StreamingGifWriter.GifStream gif = writer.prepareStream(outputFile, BufferedImage.TYPE_INT_ARGB)) {
            for (DelayedImage frame : frames) {
                gif.writeFrame(ImmutableImage.wrapAwt(frame.getImage()), Duration.ofMillis(frame.getDelay()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error writing GIF file", e);
        }
    }
}
