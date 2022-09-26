package io.github.shaksternano.mediamanipulator.image.util;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.imagemedia.StaticImage;
import io.github.shaksternano.mediamanipulator.image.reader.util.ImageReaders;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains static methods for dealing with images.
 */
public class ImageUtil {

    public static ImageMedia getImageResourceInRootPackage(String imageResourcePath) throws IOException {
        try (InputStream imageTypeInputStream = FileUtil.getResourceInRootPackage(imageResourcePath)) {
            String imageFormat = getImageFormat(imageTypeInputStream);

            try (InputStream loadImageInputStream = FileUtil.getResourceInRootPackage(imageResourcePath)) {
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
        return ImmutableImage.wrapAwt(toFit).max(width, height).awt();
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
     * @param image1             The first image.
     * @param image2             The second image.
     * @param image1IsBackground Whether the first image is the background or not. If the first image is not the background, then the second image is.
     * @param x2                 The x coordinate of the top left corner of the second image in relation to the top left corner of the first image.
     * @param y2                 The y coordinate of the top left corner of the second image in relation to the top left corner of the first image.
     * @param image2Clip         The clipping area of the second image.
     * @param imageType          The type of the resulting image.
     * @param fill               The background color.
     * @param expand             Whether to expand the resulting image to fit the second image in the case that it oversteps the boundaries of the first image.
     * @return The overlaid image.
     */
    public static BufferedImage overlayImage(BufferedImage image1, BufferedImage image2, boolean image1IsBackground, int x2, int y2, @Nullable Shape image2Clip, @Nullable Integer imageType, @Nullable Color fill, boolean expand) {
        ImageMedia imageMedia1 = new StaticImage(image1);
        ImageMedia imageMedia2 = new StaticImage(image2);
        return overlayImage(imageMedia1, imageMedia2, image1IsBackground, x2, y2, image2Clip, imageType, fill, expand).getFirstImage();
    }

    @SuppressWarnings("UnusedAssignment")
    public static ImageMedia overlayImage(ImageMedia imageMedia1, ImageMedia imageMedia2, boolean image1IsBackground, int x2, int y2, @Nullable Shape image2Clip, @Nullable Integer imageType, @Nullable Color fill, boolean expand) {
        List<BufferedImage> normalisedImage1 = new ArrayList<>(imageMedia1.toNormalisedImages());
        List<BufferedImage> normalisedImage2 = new ArrayList<>(imageMedia2.toNormalisedImages());

        BufferedImage firstImage1 = imageMedia1.getFirstImage();
        BufferedImage firstImage2 = imageMedia2.getFirstImage();

        int image1Width = firstImage1.getWidth();
        int image1Height = firstImage1.getHeight();

        int image2Width = firstImage2.getWidth();
        int image2Height = firstImage2.getHeight();

        int type = imageType == null ? ImageUtil.getType(firstImage1) : imageType;

        imageMedia1 = null;
        imageMedia2 = null;

        firstImage1.flush();
        firstImage2.flush();
        firstImage1 = null;
        firstImage2 = null;

        int overlaidWidth;
        int overlaidHeight;

        int image1X;
        int image1Y;

        int image2X;
        int image2Y;

        if (expand) {
            if (x2 < 0) {
                overlaidWidth = Math.max(image1Width - x2, image2Width);
                image1X = -x2;
            } else {
                overlaidWidth = Math.max(image1Width, image2Width + x2);
                image1X = 0;
            }

            if (y2 < 0) {
                overlaidHeight = Math.max(image1Height - y2, image2Height);
                image1Y = -y2;
            } else {
                overlaidHeight = Math.max(image1Height, image2Height + y2);
                image1Y = 0;
            }

            image2X = Math.max(x2, 0);
            image2Y = Math.max(y2, 0);
        } else {
            overlaidWidth = image1Width;
            overlaidHeight = image1Height;

            image1X = 0;
            image1Y = 0;

            image2X = x2;
            image2Y = y2;
        }

        ImageMediaBuilder builder = new ImageMediaBuilder();

        BufferedImage previousImage1 = null;
        BufferedImage previousImage2 = null;

        int size = Math.max(normalisedImage1.size(), normalisedImage2.size());
        for (int i = 0; i < size; i++) {
            BufferedImage image1 = normalisedImage1.get(i % normalisedImage1.size());
            BufferedImage image2 = normalisedImage2.get(i % normalisedImage2.size());

            int remaining = size - i;
            if (normalisedImage1.size() - remaining >= 0) {
                normalisedImage1.set(i % normalisedImage1.size(), null);
            }
            if (normalisedImage2.size() - remaining >= 0) {
                normalisedImage2.set(i % normalisedImage2.size(), null);
            }

            if (image1.equals(previousImage1) && image2.equals(previousImage2)) {
                builder.increaseLastFrameDuration(Frame.GIF_MINIMUM_FRAME_DURATION);
            } else {
                BufferedImage overlaidImage = new BufferedImage(overlaidWidth, overlaidHeight, type);
                Graphics2D graphics = overlaidImage.createGraphics();

                if (fill != null) {
                    graphics.setColor(fill);

                    if (image2Clip == null) {
                        graphics.fillRect(0, 0, overlaidWidth, overlaidHeight);
                    } else {
                        graphics.fill(image2Clip);
                    }
                }

                if (!image1IsBackground) {
                    if (image2Clip != null) {
                        graphics.setClip(image2Clip);
                    }
                    graphics.drawImage(image2, image2X, image2Y, null);
                    graphics.setClip(null);

                    graphics.drawImage(image1, image1X, image1Y, null);
                } else {
                    graphics.drawImage(image1, image1X, image1Y, null);

                    if (image2Clip != null) {
                        graphics.setClip(image2Clip);
                    }
                    graphics.drawImage(image2, image2X, image2Y, null);
                    graphics.setClip(null);
                }

                graphics.dispose();

                builder.add(new AwtFrame(overlaidImage, Frame.GIF_MINIMUM_FRAME_DURATION));

                previousImage1 = image1;
                previousImage2 = image2;
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

    public static boolean isTransparent(int rgb) {
        return (rgb >> 24) == 0;
    }

    public static int get1dIndex(int x, int y, int width) {
        return y * width + x;
    }

    public static int getX(int index, int width) {
        return index % width;
    }

    public static int getY(int index, int width) {
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        inputStream.transferTo(outputStream);
        InputStream copy = new ByteArrayInputStream(outputStream.toByteArray());
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(copy)) {
            if (imageInputStream != null) {
                Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);

                if (imageReaders.hasNext()) {
                    ImageReader reader = imageReaders.next();
                    return reader.getFormatName();
                }
            }

            throw new IllegalArgumentException("Unable to determine image type");
        }
    }

    public static String getImageFormat(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return getImageFormat(inputStream);
        }
    }

    public static String getImageFormat(URL url) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return getImageFormat(inputStream);
        }
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
        return type < 1 || type > 13 ? BufferedImage.TYPE_INT_ARGB : type;
    }

    public static Area getArea(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        boolean cont = false;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (isTransparent(image.getRGB(x, y))) {
                    cont = false;
                } else {
                    if (cont) {
                        path.lineTo(x, y);
                        path.lineTo(x, y + 1);
                        path.lineTo(x + 1, y + 1);
                        path.lineTo(x + 1, y);
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                    }
                    cont = true;
                }
            }
            cont = false;
        }
        path.closePath();

        return new Area(path);
    }

    public static BufferedImage floodFill(BufferedImage image, int startX, int startY, Color fill) {
        return floodFill(image, startX, startY, fill.getRGB());
    }

    public static BufferedImage floodFill(BufferedImage image, int startX, int startY, int fillRgb) {
        int currentRgb = image.getRGB(startX, startY);
        if (currentRgb == fillRgb) {
            return image;
        } else {
            BufferedImage filledImage = new BufferedImage(image.getWidth(), image.getHeight(), getType(image));
            Graphics2D graphics = filledImage.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.dispose();
            floodFill(filledImage, startX, startY, currentRgb, fillRgb);
            return filledImage;
        }
    }

    private static void floodFill(BufferedImage image, int x, int y, int previousRgb, int newRgb) {
        // Recursive cases
        if (x >= 0
                && x < image.getWidth()
                && y >= 0
                && y < image.getHeight()
                && image.getRGB(x, y) == previousRgb
        ) {
            image.setRGB(x, y, newRgb);

            // Recur for north, east, south and west
            floodFill(image, x + 1, y, previousRgb, newRgb);
            floodFill(image, x - 1, y, previousRgb, newRgb);
            floodFill(image, x, y + 1, previousRgb, newRgb);
            floodFill(image, x, y - 1, previousRgb, newRgb);
        }
    }

    /**
     * Gets the distance between two colors.
     *
     * @param color1 The first color.
     * @param color2 The second color.
     * @return A value between 0 and 765 representing the distance between the two colors.
     */
    public static double colorDistance(Color color1, Color color2) {
        if (color1.equals(color2)) {
            return 0;
        } else {
            int red1 = color1.getRed();
            int red2 = color2.getRed();
            int redMean = (red1 + red2) / 2;
            int redDifference = red1 - red2;
            int greenDifference = color1.getGreen() - color2.getGreen();
            int blueDifference = color1.getBlue() - color2.getBlue();
            return Math.sqrt((((512 + redMean) * redDifference * redDifference) >> 8) + 4 * greenDifference * greenDifference + (((767 - redMean) * blueDifference * blueDifference) >> 8));
        }
    }

    public static boolean isGreyScale(Color color) {
        int tolerance = 20;
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return Math.abs(red - green) <= tolerance && Math.abs(red - blue) <= tolerance && Math.abs(green - blue) <= tolerance;
    }

    public static BufferedImage copy(BufferedImage image) {
        BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), getType(image));
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }
}
