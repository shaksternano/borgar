package io.github.shaksternano.mediamanipulator.media.io.writer;

import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.media.ImageFrame;
import io.github.shaksternano.mediamanipulator.media.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ScrimageGifWriter extends NoAudioWriter {

    /**
     * The minimum frame duration in microseconds allowed on GIF files.
     */
    private static final int GIF_MINIMUM_FRAME_DURATION = 20000;

    private final StreamingGifWriter.GifStream gif;

    // For optimising transparency.
    @Nullable
    private BufferedImage previousImage;
    private boolean cannotOptimiseNext;

    // For optimising duplicate sequential frames, and dealing with the minimum frame duration.
    /**
     * The {@link #previousImage} after transparency optimisation.
     */
    @Nullable
    private BufferedImage pendingWrite;
    private double pendingDuration;
    private DisposeMethod pendingDisposeMethod = DisposeMethod.NONE;

    private boolean closed;

    public ScrimageGifWriter(File output) throws IOException {
        var writer = new StreamingGifWriter();
        gif = writer.prepareStream(output, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void recordImageFrame(ImageFrame frame) throws IOException {
        var currentImage = ImageUtil.convertType(frame.content(), BufferedImage.TYPE_INT_ARGB);
        if (ImageUtil.imageEquals(previousImage, currentImage)) {
            // Merge duplicate sequential frames into one.
            pendingDuration += frame.duration();
        } else {
            // Write the previous frame if it exists and the duration is long enough.
            if (pendingWrite != null && pendingDuration >= GIF_MINIMUM_FRAME_DURATION) {
                writeFrame();
                pendingWrite = null;
            }

            // Optimise transparency.
            BufferedImage toWrite;
            DisposeMethod disposeMethod;
            if (previousImage == null) {
                toWrite = currentImage;
                disposeMethod = DisposeMethod.NONE;
            } else if (cannotOptimiseNext) {
                toWrite = currentImage;
                if (fullyOpaque(currentImage)) {
                    disposeMethod = DisposeMethod.NONE;
                    cannotOptimiseNext = false;
                } else {
                    disposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR;
                }
            } else {
                try {
                    toWrite = optimiseTransparency(previousImage, currentImage);
                    disposeMethod = DisposeMethod.DO_NOT_DISPOSE;
                } catch (PreviousTransparentException ignored) {
                    toWrite = currentImage;
                    disposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR;
                    cannotOptimiseNext = true;
                }
            }

            if (pendingWrite == null) {
                previousImage = currentImage;

                pendingWrite = toWrite;
                pendingDuration = frame.duration();
                pendingDisposeMethod = disposeMethod;
            } else {
                // Handle the minimum frame duration.
                var remainingDuration = GIF_MINIMUM_FRAME_DURATION - pendingDuration;
                if (remainingDuration < frame.duration()) {
                    writeFrameMinimumDuration();

                    previousImage = currentImage;

                    pendingWrite = toWrite;
                    pendingDuration = frame.duration() - remainingDuration;
                    pendingDisposeMethod = disposeMethod;
                } else {
                    pendingDuration += frame.duration();
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            try {
                if (pendingWrite != null) {
                    writeFrameMinimumDuration();
                }
                gif.close();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    private static BufferedImage optimiseTransparency(
        BufferedImage previousImage,
        BufferedImage currentImage
    ) throws PreviousTransparentException {
        var colorTolerance = 10;
        List<Position> similarPixels = new ArrayList<>();
        for (var x = 0; x < previousImage.getWidth(); x++) {
            for (var y = 0; y < previousImage.getHeight(); y++) {
                var previousPixelColor = new Color(previousImage.getRGB(x, y), true);
                var currentPixelColor = new Color(currentImage.getRGB(x, y), true);
                if (currentPixelColor.getAlpha() == 0 && previousPixelColor.getAlpha() != 0) {
                    throw new PreviousTransparentException();
                } else {
                    var colorDistance = ImageUtil.colorDistance(previousPixelColor, currentPixelColor);
                    if (colorDistance <= colorTolerance) {
                        similarPixels.add(new Position(x, y));
                    }
                }
            }
        }
        return removePixels(currentImage, similarPixels);
    }

    private static BufferedImage removePixels(BufferedImage image, Iterable<Position> toRemove) {
        var newImage = ImageUtil.copy(image);
        for (var position : toRemove) {
            newImage.setRGB(position.x(), position.y(), 0);
        }
        return newImage;
    }

    private static boolean fullyOpaque(BufferedImage image) {
        for (var x = 0; x < image.getWidth(); x++) {
            for (var y = 0; y < image.getHeight(); y++) {
                var pixelColor = new Color(image.getRGB(x, y), true);
                if (pixelColor.getAlpha() < 255) {
                    return false;
                }
            }
        }
        return true;
    }

    private void writeFrame() throws IOException {
        checkPendingFrame();
        writeFrame(gif, pendingWrite, pendingDuration, pendingDisposeMethod);
    }

    private void writeFrameMinimumDuration() throws IOException {
        checkPendingFrame();
        writeFrame(gif, pendingWrite, GIF_MINIMUM_FRAME_DURATION, pendingDisposeMethod);
    }

    private void checkPendingFrame() {
        if (pendingWrite == null) {
            throw new IllegalStateException("No frame to write");
        }
    }

    /**
     * Write a frame to the GIF.
     *
     * @param gif           The GIF to write to.
     * @param image         The image to write.
     * @param duration      The duration of the frame in microseconds.
     * @param disposeMethod The dispose method to use.
     * @throws IOException If an I/O error occurs.
     */
    private static void writeFrame(
        StreamingGifWriter.GifStream gif,
        BufferedImage image,
        double duration,
        DisposeMethod disposeMethod
    ) throws IOException {
        var immutableImage = ImmutableImage.wrapAwt(image);
        var frameDuration = Duration.of(Math.max((long) duration, GIF_MINIMUM_FRAME_DURATION), ChronoUnit.MICROS);
        gif.writeFrame(immutableImage, frameDuration, disposeMethod);
    }

    private record Position(int x, int y) {
    }

    /**
     * Indicates that the previous frame had a transparent pixel
     * at a position where the current frame has an opaque pixel,
     * which means that the current frame cannot be optimised.
     */
    private static class PreviousTransparentException extends Exception {
    }
}
