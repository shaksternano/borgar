package io.github.shaksternano.mediamanipulator.util;

import com.sksamuel.scrimage.DisposeMethod;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.nio.AnimatedGif;
import com.sksamuel.scrimage.nio.AnimatedGifReader;
import com.sksamuel.scrimage.nio.ImageSource;
import com.sksamuel.scrimage.nio.StreamingGifWriter;
import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.CompositeDrawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.*;

/**
 * Contains static methods for dealing with images.
 */
public class ImageUtil {

    public static BufferedImage getImageResource(String resourcePath) throws IOException {
        try (InputStream imageStream = FileUtil.getResource(resourcePath)) {
            return readImage(imageStream);
        }
    }

    /**
     * Adds a caption to an image.
     *
     * @param image        The image to add a caption to.
     * @param words        The words of the caption.
     * @param font         The font to use for the caption.
     * @param nonTextParts The non text parts to use in the caption.
     * @return The image with the caption added.
     */
    public static BufferedImage captionImage(BufferedImage image, String[] words, Font font, Map<String, Drawable> nonTextParts) {
        font = font.deriveFont(image.getWidth() / 10F);
        int padding = (int) (image.getWidth() * 0.04);
        Graphics2D graphics = image.createGraphics();

        configureTextSettings(graphics);

        graphics.setFont(font);

        CompositeDrawable paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
                .addWords(words)
                .build(TextAlignment.CENTER, image.getWidth() - (padding * 2), null);

        int fillHeight = paragraph.getHeight(graphics) + (padding * 2);
        graphics.dispose();

        BufferedImage resizedImage = new BufferedImage(image.getWidth(), image.getHeight() + fillHeight, image.getType());

        graphics = resizedImage.createGraphics();
        graphics.setFont(font);

        graphics.drawImage(image, 0, fillHeight, null);

        configureTextSettings(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

        graphics.setColor(Color.BLACK);

        paragraph.draw(graphics, padding, padding);

        graphics.dispose();
        return resizedImage;
    }

    private static void configureTextSettings(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );
    }

    /**
     * Stretches an image.
     *
     * @param image        The image to stretch.
     * @param targetWidth  The width to stretch the image to.
     * @param targetHeight The height to stretch the image to.
     * @param raw          If false, extra processing is done to smoothen the resulting image.
     *                     If true, no extra processing is done.
     * @return The stretched image.
     */
    public static BufferedImage stretch(BufferedImage image, int targetWidth, int targetHeight, boolean raw) {
        if (raw) {
            BufferedImage stretchedImage = new BufferedImage(targetWidth, targetHeight, image.getType());
            Graphics2D graphics = stretchedImage.createGraphics();
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
            return stretchedImage;
        }

        return ImmutableImage.wrapAwt(image).scaleTo(targetWidth, targetHeight).awt();
    }

    public static BufferedImage resize(BufferedImage image, float resizeMultiplier, boolean raw) {
        return stretch(image, (int) (image.getWidth() * resizeMultiplier), (int) (image.getHeight() * resizeMultiplier), raw);
    }

    public static BufferedImage pixelate(BufferedImage image, int pixelationMultiplier) {
        return stretch(stretch(image, image.getWidth() / pixelationMultiplier, image.getHeight() / pixelationMultiplier, true), image.getWidth(), image.getHeight(), true);
    }

    public static BufferedImage fitWidth(BufferedImage toFit, int width) {
        return ImmutableImage.wrapAwt(toFit).scaleToWidth(width).awt();
    }

    public static BufferedImage fitHeight(BufferedImage toFit, int height) {
        return ImmutableImage.wrapAwt(toFit).scaleToHeight(height).awt();
    }

    public static BufferedImage fill(BufferedImage toFill, Color color) {
        BufferedImage filledImage = new BufferedImage(toFill.getWidth(), toFill.getHeight(), toFill.getType());
        Graphics2D graphics = filledImage.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, filledImage.getWidth(), filledImage.getHeight());
        graphics.drawImage(toFill, 0, 0, null);
        graphics.dispose();
        return filledImage;
    }

    /**
     * Overlays an image on top of another image.
     *
     * @param image       The image being overlaid on.
     * @param overlay     The image to overlay.
     * @param x           The x coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param y           The y coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param expand      Whether to expand the resulting image to fit the overlay image.
     * @param expandColor The background color used if the resulting image is expanded.
     * @return The overlaid image.
     */
    public static BufferedImage overlayImage(BufferedImage image, BufferedImage overlay, int x, int y, boolean expand, @Nullable Color expandColor) {
        if (expand) {
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            int overlayWidth = overlay.getWidth();
            int overlayHeight = overlay.getHeight();

            int overlaidWidth = x < 0 ? Math.max(imageWidth - x, overlayWidth) : Math.max(imageWidth, overlayWidth + x);
            int overlaidHeight = y < 0 ? Math.max(imageHeight - y, overlayHeight) : Math.max(imageHeight, overlayHeight + y);

            BufferedImage overlaidImage = new BufferedImage(overlaidWidth, overlaidHeight, image.getType());
            Graphics2D graphics = overlaidImage.createGraphics();

            if (expandColor != null) {
                graphics.setColor(expandColor);
                graphics.fillRect(0, 0, overlaidImage.getWidth(), overlaidImage.getHeight());
            }

            int imageActualX = x < 0 ? -x : 0;
            int imageActualY = y < 0 ? -y : 0;

            int overlayActualX = Math.max(x, 0);
            int overlayActualY = Math.max(y, 0);

            graphics.drawImage(image, imageActualX, imageActualY, null);
            graphics.drawImage(overlay, overlayActualX, overlayActualY, null);
            graphics.dispose();
            return overlaidImage;
        } else {
            Graphics2D graphics = image.createGraphics();
            graphics.drawImage(overlay, x, y, null);
            graphics.dispose();
            return image;
        }
    }

    public static BufferedImage cutoutImage(BufferedImage imageToCut, BufferedImage imageToCutout, int x, int y, int cutoutColor) {
        if (cutoutColor > 0xFFFFFF) {
            throw new IllegalArgumentException("Cutout color must be a 24-bit color!");
        } else {
            int toCutWidth = imageToCut.getWidth();
            int toCutHeight = imageToCut.getHeight();

            int toCutoutWidth = imageToCutout.getWidth();
            int toCutoutHeight = imageToCutout.getHeight();

            int[] toCutPixels = imageToCut.getRGB(0, 0, toCutWidth, toCutHeight, null, 0, toCutWidth);
            int[] toCutoutPixels = imageToCutout.getRGB(0, 0, toCutoutWidth, toCutoutHeight, null, 0, toCutoutWidth);

            for (int i = 0; i < toCutoutPixels.length; i++) {
                int toCutoutRgb = toCutoutPixels[i];

                if (!isTransparent(toCutoutRgb)) {
                    int toCutIndex = get1dIndex(Math.min(toCutWidth, x + getX(i, toCutWidth)), Math.min(toCutHeight, y + getY(i, toCutWidth)), toCutWidth);

                    if (toCutIndex < toCutPixels.length) {
                        toCutPixels[toCutIndex] = cutoutColor;
                    }
                }
            }

            imageToCut.setRGB(0, 0, toCutWidth, toCutHeight, toCutPixels, 0, toCutWidth);
            return imageToCut;
        }
    }

    private static boolean isTransparent(int rgb) {
        return (rgb >> 24) == 0x00;
    }

    private static int get1dIndex(int x, int y, int width) {
        return y * width + x;
    }

    private static int getX(int index, int width) {
        return index % width;
    }

    private static int getY(int index, int width) {
        return index / width;
    }

    public static BufferedImage rotate(BufferedImage image, float angle, @Nullable Integer newWidth, @Nullable Integer newHeight, @Nullable Color backgroundColor) {
        double sin = Math.abs(Math.sin(Math.toRadians(angle)));
        double cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int width = image.getWidth();
        int height = image.getHeight();

        if (newWidth == null) {
            newWidth = (int) Math.floor(width * cos + height * sin);
        }

        if (newHeight == null) {
            newHeight = (int) Math.floor(height * cos + width * sin);
        }

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D graphics = rotated.createGraphics();

        if (backgroundColor != null) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, newWidth, newHeight);
        }

        graphics.translate((newWidth - width) / 2, (newHeight - height) / 2);
        graphics.rotate(Math.toRadians(angle), width / 2F, height / 2F);
        graphics.drawRenderedImage(image, null);
        graphics.dispose();

        return rotated;
    }

    /**
     * Gets the frames of a GIF file.
     *
     * @param media The GIF file to get the frames of.
     * @return A list of {@link DurationImage}s representing the frames of the GIF file.
     * @throws IOException If an error occurs while reading the GIF file.
     */
    public static List<DurationImage> readGifFrames(File media) throws IOException {
        List<DurationImage> frames = new ArrayList<>();
        AnimatedGif gif = AnimatedGifReader.read(ImageSource.of(media));

        for (int i = 0; i < gif.getFrameCount(); i++) {
            BufferedImage frame = gif.getFrame(i).awt();
            int duration = (int) gif.getDelay(i).toMillis();
            frames.add(new DurationImage(frame, duration));
        }

        return frames;
    }

    /**
     * Writes the given frames to a GIF file.
     *
     * @param frames     The {@link DurationImage} frames to write to the GIF file.
     * @param outputFile The file to write the frames to.
     */
    public static void writeFramesToGifFile(Iterable<DurationImage> frames, File outputFile) {
        StreamingGifWriter writer = new StreamingGifWriter();
        try (StreamingGifWriter.GifStream gif = writer.prepareStream(outputFile, BufferedImage.TYPE_INT_ARGB)) {
            for (DurationImage frame : frames) {
                gif.writeFrame(ImmutableImage.wrapAwt(frame.getImage()), Duration.ofMillis(frame.getDuration()), DisposeMethod.RESTORE_TO_BACKGROUND_COLOR);
                frame.getImage().flush();
            }
        } catch (Exception e) {
            Main.getLogger().error("Error writing GIF file", e);
        }
    }

    public static Optional<String> getImageType(File file) throws IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(file)) {
            if (imageInputStream != null) {
                Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

                if (imageReaders.hasNext()) {
                    ImageReader reader = imageReaders.next();
                    return Optional.of(reader.getFormatName());
                }
            }

            return Optional.empty();
        }
    }

    public static BufferedImage readImage(File file) throws IOException {
        try {
            return ImmutableImage.loader().fromFile(file).awt();
        } catch (IOException e) {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                throw new IOException("Could not read image file!");
            } else {
                return image;
            }
        }
    }

    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        try {
            return ImmutableImage.loader().fromStream(inputStream).awt();
        } catch (IOException e) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new IOException("Could not read image file!");
            } else {
                return image;
            }
        }
    }

    public static BufferedImage readImage(URL url) throws IOException {
        BufferedImage image = ImageIO.read(url);
        if (image == null) {
            throw new IOException("Could not read image file from URL: " + url);
        } else {
            return image;
        }
    }

    public static BufferedImage readImageWithAlpha(File file) throws IOException {
        BufferedImage originalImage = readImage(file);
        BufferedImage imageWithAlpha = addAlpha(originalImage);
        originalImage.flush();
        return imageWithAlpha;
    }

    public static BufferedImage addAlpha(BufferedImage image) {
        return convertType(image, BufferedImage.TYPE_INT_ARGB);
    }

    private static BufferedImage convertType(BufferedImage image, int type) {
        BufferedImage imageWithAlpha = new BufferedImage(image.getWidth(), image.getHeight(), type);
        ColorConvertOp convertOp = new ColorConvertOp(null);
        return convertOp.filter(image, imageWithAlpha);
    }
}
