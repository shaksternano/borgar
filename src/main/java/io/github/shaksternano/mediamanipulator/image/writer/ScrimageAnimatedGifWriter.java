package io.github.shaksternano.mediamanipulator.image.writer;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.GifWriter;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.Frame;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ScrimageAnimatedGifWriter implements ImageWriter {

    @Override
    public void write(ImageMedia image, File file, String format) throws IOException {
        if (image.isAnimated()) {
            StreamingGifWriter writer = new StreamingGifWriter();
            try (StreamingGifWriter.GifStream gif = writer.prepareStream(file, BufferedImage.TYPE_INT_ARGB)) {
                BufferedImage previousImage = null;
                boolean cannotOptimiseNext = false;
                for (Frame frame : image) {
                    BufferedImage currentImage = ImageUtil.convertType(frame.getImage(), BufferedImage.TYPE_INT_ARGB);
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
                        } catch (CannotBeOptimisedException ignored) {
                            toWrite = currentImage;
                            disposeMethod = DisposeMethod.RESTORE_TO_BACKGROUND_COLOR;
                            cannotOptimiseNext = true;
                        }
                    }
                    ImmutableImage immutableImage = ImmutableImage.wrapAwt(toWrite);
                    gif.writeFrame(immutableImage, Duration.ofMillis(frame.getDuration()), disposeMethod);
                    previousImage = currentImage;
                }
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            ImmutableImage immutableImage = ImmutableImage.wrapAwt(image.getFirstImage());
            immutableImage.output(GifWriter.Default, file);
        }
    }

    @Override
    public Set<String> getSupportedFormats() {
        return ImmutableSet.of(
            "gif"
        );
    }

    private static BufferedImage optimiseTransparency(BufferedImage previousImage, BufferedImage currentImage) throws CannotBeOptimisedException {
        int colorTolerance = 10;
        List<Position> similarPixels = new ArrayList<>();
        for (int x = 0; x < previousImage.getWidth(); x++) {
            for (int y = 0; y < previousImage.getHeight(); y++) {
                Color previousPixelColor = new Color(previousImage.getRGB(x, y), true);
                Color currentPixelColor = new Color(currentImage.getRGB(x, y), true);
                if (currentPixelColor.getAlpha() == 0 && previousPixelColor.getAlpha() != 0) {
                    throw new CannotBeOptimisedException();
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

    private static class CannotBeOptimisedException extends Exception {
    }
}
