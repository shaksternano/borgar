package io.github.shaksternano.mediamanipulator.image.util;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;
import io.github.shaksternano.mediamanipulator.image.io.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains static methods for dealing with images.
 */
public class ImageUtil {

    public static ImageMedia getImageResourceInRootPackage(String resourcePath) throws IOException {
        try (InputStream imageTypeInputStream = FileUtil.getResourceInRootPackage(resourcePath)) {
            String imageFormat = getImageFormat(imageTypeInputStream);

            try (InputStream loadImageInputStream = FileUtil.getResourceInRootPackage(resourcePath)) {
                return ImageReaders.read(loadImageInputStream, imageFormat, null);
            }
        }
    }

    public static void configureTextDrawQuality(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON

        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
    }

    public static void drawOutlinedText(Graphics2D graphics, String text, int x, int y, Color textFillColor, Color textOutlineColor) {
        BasicStroke outlineStroke = new BasicStroke(2.0f);

        Color originalColor = graphics.getColor();
        Stroke originalStroke = graphics.getStroke();
        RenderingHints originalHints = graphics.getRenderingHints();

        AffineTransform transform = graphics.getTransform();
        double originalX = transform.getTranslateX();
        double originalY = transform.getTranslateY();

        Font font = graphics.getFont();
        FontRenderContext fontRenderContext = graphics.getFontRenderContext();
        GlyphVector glyphVector = font.createGlyphVector(fontRenderContext, text);
        Shape textShape = glyphVector.getOutline();

        configureTextDrawQuality(graphics);

        graphics.setColor(textOutlineColor);
        graphics.setStroke(outlineStroke);
        graphics.translate(x, y);
        graphics.draw(textShape);

        graphics.setColor(textFillColor);
        graphics.fill(textShape);

        graphics.setColor(originalColor);
        graphics.setStroke(originalStroke);
        graphics.setRenderingHints(originalHints);
        graphics.translate(originalX, originalY);
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
            BufferedImage stretchedImage = new BufferedImage(targetWidth, targetHeight, ImageUtil.getType(image));
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

    public static BufferedImage fitWidth(BufferedImage toFit, int width) {
        return ImmutableImage.wrapAwt(toFit).scaleToWidth(width).awt();
    }

    public static BufferedImage fitHeight(BufferedImage toFit, int height) {
        return ImmutableImage.wrapAwt(toFit).scaleToHeight(height).awt();
    }

    public static BufferedImage fit(BufferedImage toFit, int width, int height) {
        float widthToHeightRatio = (float) toFit.getWidth() / toFit.getHeight();
        float targetWidthToHeightRatio = (float) width / height;
        if (widthToHeightRatio > targetWidthToHeightRatio) {
            return fitWidth(toFit, width);
        } else {
            return fitHeight(toFit, height);
        }
    }

    public static BufferedImage fill(BufferedImage toFill, Color color) {
        BufferedImage filledImage = new BufferedImage(toFill.getWidth(), toFill.getHeight(), ImageUtil.getType(toFill));
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
     * @param background      The image being overlaid on.
     * @param overlay         The image to overlay.
     * @param x               The x coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param y               The y coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param imageType       The type of the resulting image.
     * @param fill            The background color.
     * @param expand          Whether to expand the resulting image to fit the overlay image.
     * @param invertDrawOrder Whether to draw the background image first.
     * @return The overlaid image.
     */
    public static BufferedImage overlayImage(BufferedImage background, BufferedImage overlay, int x, int y, @Nullable Integer imageType, @Nullable Color fill, boolean expand, boolean invertDrawOrder) {
        ImageMedia backgroundImage = new StaticImage(background);
        ImageMedia overlayImage = new StaticImage(overlay);
        return overlayImage(backgroundImage, overlayImage, x, y, imageType, fill, expand, false).getFrame(0).getImage();
    }

    @SuppressWarnings("UnusedAssignment")
    public static ImageMedia overlayImage(ImageMedia background, ImageMedia overlay, int x, int y, @Nullable Integer imageType, @Nullable Color fill, boolean expand, boolean invertDrawOrder) {
        List<BufferedImage> normalisedBackgroundImages = new ArrayList<>(background.toNormalisedImages());
        List<BufferedImage> normalisedOverlayImages = new ArrayList<>(overlay.toNormalisedImages());

        BufferedImage firstBackground = normalisedBackgroundImages.get(0);
        BufferedImage firstOverlay = normalisedOverlayImages.get(0);

        int backgroundWidth = firstBackground.getWidth();
        int backgroundHeight = firstBackground.getHeight();

        int overlayWidth = firstOverlay.getWidth();
        int overlayHeight = firstOverlay.getHeight();

        int type = imageType == null ? ImageUtil.getType(firstBackground) : imageType;

        background = null;
        overlay = null;

        firstBackground.flush();
        firstOverlay.flush();
        firstBackground = null;
        firstOverlay = null;

        int overlaidWidth;
        int overlaidHeight;

        int backgroundX;
        int backgroundY;

        int overlayX;
        int overlayY;

        if (expand) {
            if (x < 0) {
                overlaidWidth = Math.max(backgroundWidth - x, overlayWidth);
                backgroundX = -x;
            } else {
                overlaidWidth = Math.max(backgroundWidth, overlayWidth + x);
                backgroundX = 0;
            }

            if (y < 0) {
                overlaidHeight = Math.max(backgroundHeight - y, overlayHeight);
                backgroundY = -y;
            } else {
                overlaidHeight = Math.max(backgroundHeight, overlayHeight + y);
                backgroundY = 0;
            }

            overlayX = Math.max(x, 0);
            overlayY = Math.max(y, 0);
        } else {
            overlaidWidth = backgroundWidth;
            overlaidHeight = backgroundHeight;

            backgroundX = 0;
            backgroundY = 0;

            overlayX = x;
            overlayY = y;
        }

        ImageMediaBuilder builder = new ImageMediaBuilder();

        BufferedImage previousBackground = null;
        BufferedImage previousOverlay = null;

        int size = Math.max(normalisedBackgroundImages.size(), normalisedOverlayImages.size());
        for (int i = 0; i < size; i++) {
            BufferedImage overlaidImage = new BufferedImage(overlaidWidth, overlaidHeight, type);
            Graphics2D graphics = overlaidImage.createGraphics();

            if (fill != null) {
                graphics.setColor(fill);
                graphics.fillRect(0, 0, overlaidWidth, overlaidHeight);
            }

            BufferedImage backgroundImage = normalisedBackgroundImages.get(i % normalisedBackgroundImages.size());
            BufferedImage overlayImage = normalisedOverlayImages.get(i % normalisedOverlayImages.size());

            int remaining = size - i;
            if (normalisedBackgroundImages.size() - remaining >= 0) {
                normalisedBackgroundImages.set(i % normalisedBackgroundImages.size(), null);
            }
            if (normalisedOverlayImages.size() - remaining >= 0) {
                normalisedOverlayImages.set(i % normalisedOverlayImages.size(), null);
            }

            if (backgroundImage.equals(previousBackground) && overlayImage.equals(previousOverlay)) {
                builder.increaseLastFrameDuration(Frame.GIF_MINIMUM_FRAME_DURATION);
            } else {
                if (invertDrawOrder) {
                    graphics.drawImage(overlayImage, overlayX, overlayY, null);
                    graphics.drawImage(backgroundImage, backgroundX, backgroundY, null);
                } else {
                    graphics.drawImage(backgroundImage, backgroundX, backgroundY, null);
                    graphics.drawImage(overlayImage, overlayX, overlayY, null);
                }

                graphics.dispose();

                builder.add(new AwtFrame(overlaidImage, Frame.GIF_MINIMUM_FRAME_DURATION));

                previousBackground = backgroundImage;
                previousOverlay = overlayImage;
            }
        }

        return builder.build();
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

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, ImageUtil.getType(image));
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

    public static String getImageFormat(InputStream inputStream) throws IOException {
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            if (imageInputStream != null) {
                Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

                if (imageReaders.hasNext()) {
                    ImageReader reader = imageReaders.next();
                    return reader.getFormatName();
                }
            }

            throw new IOException("Unable to determine image type");
        }
    }

    public static String getImageFormat(File file) throws IOException {
        return getImageFormat(new FileInputStream(file));
    }

    public static String getImageFormat(URL url) throws IOException {
        return getImageFormat(url.openStream());
    }

    public static BufferedImage convertType(BufferedImage image, int type) {
        if (image.getType() == type) {
            return image;
        } else {
            BufferedImage imageWithAlpha = new BufferedImage(image.getWidth(), image.getHeight(), type);
            ColorConvertOp convertOp = new ColorConvertOp(null);
            return convertOp.filter(image, imageWithAlpha);
        }
    }

    public static String imageToString(BufferedImage image) {
        return image.getClass().getSimpleName() +
                "[" +
                image.getWidth() +
                "x" +
                image.getHeight() +
                "]";
    }

    public static int getType(BufferedImage image) {
        int type = image.getType();
        return type <= 0 ? BufferedImage.TYPE_INT_ARGB : type;
    }
}
