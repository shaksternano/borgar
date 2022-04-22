package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.util.CollectionUtil;
import io.github.shaksternano.mediamanipulator.util.DelayedImage;
import io.github.shaksternano.mediamanipulator.util.FileUtil;
import io.github.shaksternano.mediamanipulator.util.MediaCompression;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * A manipulator for GIF files.
 */
public class GifManipulator extends ImageBasedManipulator {

    @Override
    public File speed(File media, float speedMultiplier) throws IOException {
        List<DelayedImage> frames = readGifFrames(media);
        frames = MediaCompression.removeFrames(frames, media.length(), FileUtil.DISCORD_MAXIMUM_FILE_SIZE);
        List<DelayedImage> newFrames = changeSpeed(frames, speedMultiplier);
        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_changed_speed").getName());
        writeFramesToGifFile(newFrames, gifFile);
        return gifFile;
    }

    @Override
    public File reduceFps(File media, int fpsReductionRatio) throws IOException {
        List<DelayedImage> frames = readGifFrames(media);
        frames = MediaCompression.removeFrames(frames, fpsReductionRatio);
        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_fps_reduced").getName());
        writeFramesToGifFile(frames, gifFile);
        return gifFile;
    }

    /**
     * This file is already a GIF file, so we don't need to do anything.
     *
     * @param media The media file to turn into a GIF.
     * @return The media as a GIF file.
     */
    @Override
    public File makeGif(File media, boolean fallback) {
        throw new UnsupportedOperationException("This file is already a GIF file!");
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "gif"
        );
    }

    /**
     * Applies the given operation to every frame of the GIF file.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @Override
    protected File applyOperation(File media, Function<BufferedImage, BufferedImage> operation, String operationName) throws IOException {
        List<DelayedImage> frames = readGifFrames(media);
        frames = MediaCompression.removeFrames(frames, media.length(), FileUtil.DISCORD_MAXIMUM_FILE_SIZE);

        frames.parallelStream().forEach(
                delayedImage -> {
                    BufferedImage uneditedImage = delayedImage.getImage();
                    BufferedImage image = operation.apply(uneditedImage);
                    delayedImage.setImage(image);
                    uneditedImage.flush();
                }
        );

        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());
        writeFramesToGifFile(frames, gifFile);
        return gifFile;
    }

    /**
     * Gets the frames of a GIF file.
     *
     * @param media The GIF file to get the frames of.
     * @return A list of {@link DelayedImage}s representing the frames of the GIF file.
     * @throws IOException If an error occurs while reading the GIF file.
     */
    private static List<DelayedImage> readGifFrames(File media) throws IOException {
        List<DelayedImage> frames = new ArrayList<>();
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(media));

        for (int i = 0; i < gif.getFrameCount(); i++) {
            BufferedImage frame = gif.getFrame(i).awt();
            int delay = (int) gif.getDelay(i).toMillis();
            frames.add(new DelayedImage(frame, delay));
        }

        return frames;
    }

    /**
     * Writes the given frames to a GIF file.
     *
     * @param frames     The {@link DelayedImage} frames to write to the GIF file.
     * @param outputFile The file to write the frames to.
     */
    private static void writeFramesToGifFile(List<DelayedImage> frames, File outputFile) {
        StreamingGifWriter writer = new StreamingGifWriter();
        try (StreamingGifWriter.GifStream gif = writer.prepareStream(outputFile, BufferedImage.TYPE_INT_ARGB)) {
            for (DelayedImage frame : frames) {
                gif.writeFrame(ImmutableImage.wrapAwt(frame.getImage()), Duration.ofMillis(frame.getDelay()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
            }
        } catch (Exception e) {
            Main.LOGGER.error("Error writing GIF file", e);
        }
    }

    private static List<DelayedImage> changeSpeed(List<DelayedImage> frames, float speedMultiplier) {
        if (frames.size() <= 1) {
            throw new UnsupportedOperationException("Cannot change the speed of a static image.");
        } else {
            if (speedMultiplier != 1 && speedMultiplier > 0) {
                for (DelayedImage frame : frames) {
                    frame.setDelay((int) (frame.getDelay() / speedMultiplier));
                }

                List<BufferedImage> bufferedFrames = delayedImagesToBufferedImages(frames);

                bufferedFrames = CollectionUtil.removeEveryNthElement(bufferedFrames, DelayedImage.GIF_MINIMUM_DELAY);

                List<DelayedImage> newFrames = bufferedImagesToDelayedImages(bufferedFrames);

                for (DelayedImage frame : newFrames) {
                    frame.setDelay(DelayedImage.GIF_MINIMUM_DELAY);
                }

                if (newFrames.isEmpty()) {
                    if (!frames.isEmpty()) {
                        newFrames = ImmutableList.of(frames.get(0));
                    }
                }

                return newFrames;
            } else {
                throw new IllegalArgumentException("Speed multiplier " + speedMultiplier + " is not allowed!");
            }
        }
    }

    private static List<BufferedImage> delayedImagesToBufferedImages(List<DelayedImage> delayedFrames) {
        List<BufferedImage> bufferedFrames = new ArrayList<>();

        for (DelayedImage frame : delayedFrames) {
            for (int i = 0; i < frame.getDelay(); i++) {
                bufferedFrames.add(frame.getImage());
            }
        }

        return bufferedFrames;
    }

    private static List<DelayedImage> bufferedImagesToDelayedImages(List<BufferedImage> bufferedFrames) {
        List<DelayedImage> delayedFrames = new ArrayList<>();

        for (BufferedImage frame : bufferedFrames) {
            if (delayedFrames.isEmpty()) {
                delayedFrames.add(new DelayedImage(frame, 1));
            } else {
                DelayedImage delayedImage = delayedFrames.get(delayedFrames.size() - 1);
                BufferedImage lastFrame = delayedImage.getImage();

                if (frame.equals(lastFrame)) {
                    delayedImage.setDelay(delayedImage.getDelay() + 1);
                } else {
                    delayedFrames.add(new DelayedImage(frame, 1));
                }
            }
        }

        return delayedFrames;
    }
}
