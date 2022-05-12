package io.github.shaksternano.mediamanipulator.mediamanipulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.shaksternano.mediamanipulator.command.InvalidArgumentException;
import io.github.shaksternano.mediamanipulator.util.*;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Function;

/**
 * A manipulator for GIF files.
 */
public class GifManipulator extends ImageBasedManipulator {

    @Override
    public File speed(File media, float speedMultiplier) throws IOException {
        List<DelayedImage> frames = ImageUtil.readGifFrames(media);
        List<DelayedImage> newFrames = changeSpeed(frames, speedMultiplier);
        newFrames.parallelStream().forEach(delayedImage -> delayedImage.setImage(delayedImage.getImage()));

        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_changed_speed").getName());
        ImageUtil.writeFramesToGifFile(newFrames, gifFile);
        gifFile = compress(gifFile);
        return gifFile;
    }

    @Override
    public File spin(File media, float speed, @Nullable Color backgroundColor) throws IOException {
        List<DelayedImage> frames = ImageUtil.readGifFrames(media);
        List<BufferedImage> bufferedFrames = delayedImagesToBufferedImages(frames);
        List<BufferedImage> keptFrames = CollectionUtil.keepEveryNthElement(bufferedFrames, DelayedImage.GIF_MINIMUM_DELAY, Image::flush);
        List<BufferedImage> compressedFrames = new ArrayList<>(keptFrames.size());

        for (BufferedImage frame : keptFrames) {
            compressedFrames.add(MediaCompression.reduceToDisplaySize(frame));
        }

        BufferedImage firstFrame = compressedFrames.get(0);

        int maxDimension = Math.max(firstFrame.getWidth(), firstFrame.getHeight());
        float absoluteSpeed = Math.abs(speed);

        int framesPerRotation = 150;
        if (absoluteSpeed >= 1) {
            framesPerRotation = Math.max((int) (framesPerRotation / absoluteSpeed), 1);
        }

        int size = framesPerRotation * ((compressedFrames.size() + (framesPerRotation - 1)) / framesPerRotation);
        Map<Integer, DelayedImage> indexedFrames = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            int delay = DelayedImage.GIF_MINIMUM_DELAY;
            if (absoluteSpeed < 1) {
                delay /= absoluteSpeed;
            }

            indexedFrames.put(i, new DelayedImage(compressedFrames.get(i % compressedFrames.size()), delay));
        }

        return spinFrames(indexedFrames, speed, framesPerRotation, maxDimension, media, backgroundColor);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File reduceFps(File media, int fpsReductionRatio) throws IOException {
        List<DelayedImage> frames = ImageUtil.readGifFrames(media);
        frames = MediaCompression.removeFrames(frames, fpsReductionRatio);
        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_fps_reduced").getName());

        media.delete();

        ImageUtil.writeFramesToGifFile(frames, gifFile);
        return gifFile;
    }

    /**
     * This file is already a GIF file, so we don't need to do anything.
     *
     * @param media The media file to turn into a GIF.
     * @return The media as a GIF file.
     */
    @Override
    public File makeGif(File media, boolean justRenameFile) {
        throw new UnsupportedOperationException("This file is already a GIF file!");
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return ImmutableSet.of(
                "gif"
        );
    }

    @Override
    public File compress(File media) throws IOException {
        if (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
            media = applyToEachFrame(media, MediaCompression::reduceToDisplaySize, "resized", false);

            boolean reduceResolution = true;

            while (media.length() > FileUtil.DISCORD_MAXIMUM_FILE_SIZE) {
                if (reduceResolution) {
                    media = resize(media, 0.75F, false);
                } else {
                    media = reduceFps(media, 2);
                }

                reduceResolution = !reduceResolution;
            }

        }

        return media;
    }

    /**
     * Applies the given operation to every frame of the GIF file. The original file is deleted after the operation.
     *
     * @param media         The image based file to apply the operation to.
     * @param operation     The operation to apply.
     * @param operationName The name of the operation.
     * @return The resulting file.
     * @throws IOException If an error occurs while applying the operation.
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected File applyToEachFrame(File media, Function<BufferedImage, BufferedImage> operation, String operationName, boolean compressionNeeded) throws IOException {
        List<DelayedImage> frames = ImageUtil.readGifFrames(media);

        frames.parallelStream().forEach(
                delayedImage -> {
                    BufferedImage uneditedImage = delayedImage.getImage();

                    if (compressionNeeded) {
                        uneditedImage = MediaCompression.reduceToDisplaySize(uneditedImage);
                    }

                    BufferedImage image = operation.apply(uneditedImage);
                    uneditedImage.flush();

                    delayedImage.setImage(image);
                }
        );

        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_" + operationName).getName());

        media.delete();

        ImageUtil.writeFramesToGifFile(frames, gifFile);

        if (compressionNeeded) {
            gifFile = compress(gifFile);
        }

        return gifFile;
    }

    private static List<DelayedImage> changeSpeed(Collection<DelayedImage> frames, float speedMultiplier) {
        if (frames.size() <= 1) {
            throw new UnsupportedOperationException("Cannot change the speed of a static image.");
        } else {
            if (speedMultiplier != 1 && speedMultiplier > 0) {
                for (DelayedImage frame : frames) {
                    frame.setDelay((int) (frame.getDelay() / speedMultiplier));
                }

                List<BufferedImage> bufferedFrames = delayedImagesToBufferedImages(frames);
                List<BufferedImage> keptFrames = CollectionUtil.keepEveryNthElement(bufferedFrames, DelayedImage.GIF_MINIMUM_DELAY, Image::flush);
                List<DelayedImage> newFrames = bufferedImagesToDelayedImages(keptFrames);

                for (DelayedImage frame : newFrames) {
                    frame.setDelay(frame.getDelay() * DelayedImage.GIF_MINIMUM_DELAY);
                }

                if (newFrames.isEmpty()) {
                    if (!frames.isEmpty()) {
                        newFrames = ImmutableList.of(frames.iterator().next());
                    }
                }

                return newFrames;
            } else {
                throw new InvalidArgumentException("Speed multiplier " + speedMultiplier + " is not allowed!");
            }
        }
    }

    private static List<BufferedImage> delayedImagesToBufferedImages(Iterable<DelayedImage> delayedFrames) {
        List<BufferedImage> bufferedFrames = new ArrayList<>();

        for (DelayedImage frame : delayedFrames) {
            for (int i = 0; i < frame.getDelay(); i++) {
                bufferedFrames.add(frame.getImage());
            }
        }

        return bufferedFrames;
    }

    private static List<DelayedImage> bufferedImagesToDelayedImages(Iterable<BufferedImage> bufferedFrames) {
        List<DelayedImage> delayedFrames = new ArrayList<>();

        for (BufferedImage frame : bufferedFrames) {
            if (delayedFrames.isEmpty()) {
                delayedFrames.add(new DelayedImage(frame, 1));
            } else {
                DelayedImage delayedImage = delayedFrames.get(delayedFrames.size() - 1);
                BufferedImage lastFrame = delayedImage.getImage();

                if (frame.equals(lastFrame)) {
                    delayedImage.incrementDelay();
                } else {
                    delayedFrames.add(new DelayedImage(frame, 1));
                }
            }
        }

        return delayedFrames;
    }
}
