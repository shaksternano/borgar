package io.github.shaksternano.mediamanipulator.io.mediawriter;

import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ScrimageGifWriter extends NoAudioWriter {

    private final StreamingGifWriter.GifStream gif;
    @Nullable
    private BufferedImage previousImage;
    private boolean cannotOptimiseNext;

    public ScrimageGifWriter(File output) throws IOException {
        StreamingGifWriter writer = new StreamingGifWriter();
        gif = writer.prepareStream(output, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void recordImageFrame(ImageFrame frame) throws IOException {
        BufferedImage currentImage = ImageUtil.convertType(frame.image(), BufferedImage.TYPE_INT_ARGB);
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
        ImmutableImage immutableImage = ImmutableImage.wrapAwt(toWrite);
        Duration frameDuration = Duration.ofMillis(frame.duration() / 1000);
        gif.writeFrame(immutableImage, frameDuration, disposeMethod);
        previousImage = currentImage;
    }

    @Override
    public void close() throws IOException {
        try {
            gif.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static BufferedImage optimiseTransparency(
        BufferedImage previousImage,
        BufferedImage currentImage
    ) throws PreviousTransparentException {
        int colorTolerance = 10;
        List<Position> similarPixels = new ArrayList<>();
        for (int x = 0; x < previousImage.getWidth(); x++) {
            for (int y = 0; y < previousImage.getHeight(); y++) {
                Color previousPixelColor = new Color(previousImage.getRGB(x, y), true);
                Color currentPixelColor = new Color(currentImage.getRGB(x, y), true);
                if (currentPixelColor.getAlpha() == 0 && previousPixelColor.getAlpha() != 0) {
                    throw new PreviousTransparentException();
                } else {
                    double colorDistance = ImageUtil.colorDistance(previousPixelColor, currentPixelColor);
                    if (colorDistance <= colorTolerance) {
                        similarPixels.add(new Position(x, y));
                    }
                }
            }
        }
        return removePixels(currentImage, similarPixels);
    }

    private static BufferedImage removePixels(BufferedImage image, Iterable<Position> toRemove) {
        BufferedImage newImage = ImageUtil.copy(image);
        for (Position position : toRemove) {
            newImage.setRGB(position.x(), position.y(), 0);
        }
        return newImage;
    }

    private static boolean fullyOpaque(BufferedImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color pixelColor = new Color(image.getRGB(x, y), true);
                if (pixelColor.getAlpha() < 255) {
                    return false;
                }
            }
        }
        return true;
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
