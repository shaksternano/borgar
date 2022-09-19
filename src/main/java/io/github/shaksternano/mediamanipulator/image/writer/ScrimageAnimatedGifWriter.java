package io.github.shaksternano.mediamanipulator.image.writer;

import com.google.common.collect.ImmutableSet;
import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.GifWriter;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.Frame;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;

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
                for (Frame frame : image) {
                    BufferedImage currentImage = ImageUtil.convertType(frame.getImage(), BufferedImage.TYPE_INT_ARGB);
                    if (previousImage == null) {
                        ImmutableImage immutableImage = ImmutableImage.wrapAwt(currentImage);
                        gif.writeFrame(immutableImage, Duration.ofMillis(frame.getDuration()), DisposeMethod.NONE);
                    } else {
                        WriteFrameData writeFrameData = optimiseTransparency(previousImage, currentImage);
                        ImmutableImage immutableImage = ImmutableImage.wrapAwt(writeFrameData.image());
                        gif.writeFrame(immutableImage, Duration.ofMillis(frame.getDuration()), writeFrameData.disposeMethod());
                    }
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

    private static WriteFrameData optimiseTransparency(BufferedImage previousImage, BufferedImage currentImage) {
        int colorTolerance = 10;
        List<Position> similarPixels = new ArrayList<>();
        for (int x = 0; x < previousImage.getWidth(); x++) {
            for (int y = 0; y < previousImage.getHeight(); y++) {
                Color previousPixelColor = new Color(previousImage.getRGB(x, y), true);
                Color currentPixelColor = new Color(currentImage.getRGB(x, y), true);
                if (currentPixelColor.getAlpha() == 0 && previousPixelColor.getAlpha() != 0) {
                    return new WriteFrameData(currentImage, DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
                } else {
                    double colorDistance = ImageUtil.colorDistance(previousPixelColor, currentPixelColor);
                    if (colorDistance <= colorTolerance) {
                        similarPixels.add(new Position(x, y));
                    }
                }
            }
        }
        BufferedImage optimisedImage = removePixels(currentImage, similarPixels);
        return new WriteFrameData(optimisedImage, DisposeMethod.DO_NOT_DISPOSE);
    }

    private static BufferedImage removePixels(BufferedImage image, Iterable<Position> toRemove) {
        BufferedImage newImage = ImageUtil.copy(image);
        for (Position position : toRemove) {
            newImage.setRGB(position.x(), position.y(), 0);
        }
        return newImage;
    }

    private record WriteFrameData(BufferedImage image, DisposeMethod disposeMethod) {
    }

    private record Position(int x, int y) {
    }
}
