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
        List<DurationImage> frames = ImageUtil.readGifFrames(media);
        List<DurationImage> newFrames = changeSpeed(frames, speedMultiplier);
        newFrames.parallelStream().forEach(durationImage -> durationImage.setImage(MediaCompression.reduceToDisplaySize(durationImage.getImage())));

        File gifFile = FileUtil.getUniqueTempFile(FileUtil.appendName(media, "_changed_speed").getName());
        ImageUtil.writeFramesToGifFile(newFrames, gifFile);
        gifFile = compress(gifFile);
        return gifFile;
    }

    @Override
    public File spin(File media, float speed, @Nullable Color backgroundColor) throws IOException {
        List<DurationImage> frames = ImageUtil.readGifFrames(media);
        List<BufferedImage> bufferedFrames = durationImagesToBufferedImages(frames);
        List<BufferedImage> keptFrames = CollectionUtil.keepEveryNthElement(bufferedFrames, DurationImage.GIF_MINIMUM_FRAME_DURATION, Image::flush);
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
        Map<Integer, DurationImage> indexedFrames = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            int duration = DurationImage.GIF_MINIMUM_FRAME_DURATION;
            if (absoluteSpeed < 1) {
                duration /= absoluteSpeed;
            }

            indexedFrames.put(i, new DurationImage(compressedFrames.get(i % compressedFrames.size()), duration));
        }

        return spinFrames(indexedFrames, speed, framesPerRotation, maxDimension, media, backgroundColor);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public File reduceFps(File media, int fpsReductionRatio) throws IOException {
        List<DurationImage> frames = ImageUtil.readGifFrames(media);
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
        List<DurationImage> frames = ImageUtil.readGifFrames(media);

        frames.parallelStream().forEach(
                durationImage -> {
                    BufferedImage uneditedImage = durationImage.getImage();

                    if (compressionNeeded) {
                        uneditedImage = MediaCompression.reduceToDisplaySize(uneditedImage);
                    }

                    BufferedImage image = operation.apply(uneditedImage);
                    uneditedImage.flush();

                    durationImage.setImage(image);
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

    private static List<DurationImage> changeSpeed(Collection<DurationImage> frames, float speedMultiplier) {
        if (frames.size() <= 1) {
            throw new UnsupportedOperationException("Cannot change the speed of a static image.");
        } else {
            if (speedMultiplier != 1 && speedMultiplier > 0) {
                for (DurationImage frame : frames) {
                    frame.setDuration((int) (frame.getDuration() / speedMultiplier));
                }

                List<BufferedImage> bufferedFrames = durationImagesToBufferedImages(frames);
                List<BufferedImage> keptFrames = CollectionUtil.keepEveryNthElement(bufferedFrames, DurationImage.GIF_MINIMUM_FRAME_DURATION, Image::flush);
                List<DurationImage> newFrames = bufferedImagesToDurationImages(keptFrames);

                for (DurationImage frame : newFrames) {
                    frame.setDuration(frame.getDuration() * DurationImage.GIF_MINIMUM_FRAME_DURATION);
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

    private static List<BufferedImage> durationImagesToBufferedImages(Iterable<DurationImage> durationFrames) {
        List<BufferedImage> bufferedFrames = new ArrayList<>();

        for (DurationImage frame : durationFrames) {
            for (int i = 0; i < frame.getDuration(); i++) {
                bufferedFrames.add(frame.getImage());
            }
        }

        return bufferedFrames;
    }

    private static List<DurationImage> bufferedImagesToDurationImages(Iterable<BufferedImage> bufferedFrames) {
        List<DurationImage> durationFrames = new ArrayList<>();

        for (BufferedImage frame : bufferedFrames) {
            if (durationFrames.isEmpty()) {
                durationFrames.add(new DurationImage(frame, 1));
            } else {
                DurationImage durationImage = durationFrames.get(durationFrames.size() - 1);
                BufferedImage lastFrame = durationImage.getImage();

                if (frame.equals(lastFrame)) {
                    durationImage.incrementDuration();
                } else {
                    durationFrames.add(new DurationImage(frame, 1));
                }
            }
        }

        return durationFrames;
    }
}
