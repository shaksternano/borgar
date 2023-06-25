package io.github.shaksternano.borgar.media;

import com.sksamuel.scrimage.ImmutableImage;
import io.github.shaksternano.borgar.media.graphics.GraphicsUtil;
import io.github.shaksternano.borgar.media.graphics.OverlayData;
import io.github.shaksternano.borgar.media.graphics.TextDrawData;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.graphics.drawable.ParagraphCompositeDrawable;
import io.github.shaksternano.borgar.media.template.Template;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Contains static methods for dealing with images.
 */
public class ImageUtil {

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
        if (image.getWidth() == targetWidth && image.getHeight() == targetHeight) {
            return image;
        }
        targetWidth = Math.max(targetWidth, 1);
        targetHeight = Math.max(targetHeight, 1);
        if (raw) {
            return stretchRaw(image, targetWidth, targetHeight);
        } else {
            try {
                return ImmutableImage.wrapAwt(image).scaleTo(targetWidth, targetHeight).awt();
            } catch (Exception e) {
                return stretchRaw(image, targetWidth, targetHeight);
            }
        }
    }

    private static BufferedImage stretchRaw(BufferedImage image, int targetWidth, int targetHeight) {
        var stretchedImage = new BufferedImage(targetWidth, targetHeight, ImageUtil.getType(image));
        var graphics = stretchedImage.createGraphics();
        graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return stretchedImage;
    }

    public static BufferedImage resize(BufferedImage image, float resizeMultiplier) {
        return resize(image, resizeMultiplier, false);
    }

    public static BufferedImage resize(BufferedImage image, float resizeMultiplier, boolean raw) {
        if (resizeMultiplier == 1) {
            return image;
        } else {
            return stretch(image, (int) (image.getWidth() * resizeMultiplier), (int) (image.getHeight() * resizeMultiplier), raw);
        }
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

    public static BufferedImage bound(BufferedImage image, int width, int height) {
        return ImmutableImage.wrapAwt(image).bound(width, height).awt();
    }

    public static BufferedImage bound(BufferedImage image, int maxDimension) {
        try {
            return bound(image, maxDimension, maxDimension);
        } catch (Exception e) {
            var width = image.getWidth();
            var height = image.getHeight();
            if (width <= maxDimension && height <= maxDimension) {
                return image;
            }
            float resizeRatio;
            if (width > height) {
                resizeRatio = (float) maxDimension / width;
            } else {
                resizeRatio = (float) maxDimension / height;
            }
            return resize(image, resizeRatio, true);
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
     * Calculates information used for overlaying an image on top of another image.
     *
     * @param image1    The first image.
     * @param image2    The second image.
     * @param x2        The x coordinate of the top left corner of the second image in relation to the top left corner of the first image.
     * @param y2        The y coordinate of the top left corner of the second image in relation to the top left corner of the first image.
     * @param expand    Whether to expand the resulting image to fit the second image in the case that it oversteps the boundaries of the first image.
     * @param imageType The type of the resulting image.
     * @return The overlay information.
     */
    public static OverlayData getOverlayData(
        BufferedImage image1,
        BufferedImage image2,
        int x2,
        int y2,
        boolean expand,
        @Nullable Integer imageType
    ) {
        var image1Width = image1.getWidth();
        var image1Height = image1.getHeight();

        var image2Width = image2.getWidth();
        var image2Height = image2.getHeight();

        var type = imageType == null ? ImageUtil.getType(image1) : imageType;

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

        return new OverlayData(
            overlaidWidth,
            overlaidHeight,
            image1X,
            image1Y,
            image2X,
            image2Y,
            type
        );
    }

    /**
     * Overlays an image on top of another image.
     *
     * @param image1             The first image.
     * @param image2             The second image.
     * @param overlayData        Additional information used for overlaying the images.
     * @param image1IsBackground Whether the first image is the background or not. If the first image is not the background, then the second image is.
     * @param image2Clip         The clipping area of the second image.
     * @param fill               The background color.
     * @return The overlaid image.
     */
    public static BufferedImage overlayImage(
        BufferedImage image1,
        BufferedImage image2,
        OverlayData overlayData,
        boolean image1IsBackground,
        @Nullable Shape image2Clip,
        @Nullable Color fill
    ) {
        var overlaidImage = new BufferedImage(
            overlayData.overlaidWidth(),
            overlayData.overlaidHeight(),
            overlayData.overlaidImageType()
        );
        var graphics = overlaidImage.createGraphics();

        if (fill != null) {
            graphics.setColor(fill);
            if (image2Clip == null) {
                graphics.fillRect(
                    0,
                    0,
                    overlayData.overlaidWidth(),
                    overlayData.overlaidHeight()
                );
            } else {
                graphics.fill(image2Clip);
            }
        }

        if (image1IsBackground) {
            graphics.drawImage(
                image1,
                overlayData.image1X(),
                overlayData.image1Y(),
                null
            );
            if (image2Clip != null) {
                graphics.setClip(image2Clip);
            }
            graphics.drawImage(
                image2,
                overlayData.image2X(),
                overlayData.image2Y(),
                null
            );
        } else {
            if (image2Clip != null) {
                graphics.setClip(image2Clip);
            }
            graphics.drawImage(
                image2,
                overlayData.image2X(),
                overlayData.image2Y(),
                null
            );
            graphics.setClip(null);
            graphics.drawImage(
                image1,
                overlayData.image1X(),
                overlayData.image1Y(),
                null
            );
        }

        graphics.dispose();
        return overlaidImage;
    }

    public static Optional<TextDrawData> getTextDrawData(
        BufferedImage image,
        List<String> words,
        Map<String, Drawable> nonTextParts,
        Template template
    ) {
        if (words.isEmpty()) {
            return Optional.empty();
        }

        var paragraph = new ParagraphCompositeDrawable.Builder(nonTextParts)
            .addWords(template.getCustomTextDrawableFactory().orElse(null), words)
            .build(template.getTextContentAlignment(), template.getTextContentWidth());

        var graphics = image.createGraphics();

        var font = template.getFont();
        graphics.setFont(font);
        configureTextDrawQuality(graphics);

        GraphicsUtil.fontFitWidth(template.getTextContentWidth(), paragraph, graphics);
        var paragraphHeight = GraphicsUtil.fontFitHeight(template.getTextContentHeight(), paragraph, graphics);
        var fontSize = graphics.getFont().getSize2D();

        graphics.dispose();

        var resizedFont = font.deriveFont(fontSize);

        var containerCentreY = template.getTextContentY() + (template.getTextContentHeight() / 2);

        var paragraphX = template.getTextContentX();
        var paragraphY = switch (template.getTextContentPosition()) {
            case TOP -> template.getTextContentY();
            case BOTTOM -> template.getTextContentY() + (template.getTextContentHeight() - paragraphHeight);
            default -> containerCentreY - (paragraphHeight / 2);
        };

        return Optional.of(new TextDrawData(paragraph, paragraphX, paragraphY, resizedFont));
    }

    public static BufferedImage drawText(
        BufferedImage image,
        TextDrawData textDrawData,
        long timestamp,
        Template template
    ) throws IOException {
        var imageWithText = copySize(image);
        var graphics = imageWithText.createGraphics();

        var contentClipOptional = template.getContentClip();
        template.getFill().ifPresent(color -> {
            graphics.setColor(color);
            contentClipOptional.ifPresentOrElse(
                graphics::fill,
                () -> graphics.fillRect(0, 0, imageWithText.getWidth(), imageWithText.getHeight())
            );
        });

        if (template.isBackground()) {
            graphics.drawImage(image, 0, 0, null);
        }

        var font = textDrawData.font();
        graphics.setFont(font);
        ImageUtil.configureTextDrawQuality(graphics);
        graphics.setColor(template.getTextColor());

        contentClipOptional.ifPresent(graphics::setClip);

        var textX = textDrawData.textX();
        var textY = textDrawData.textY();
        var text = textDrawData.text();
        text.draw(graphics, textX, textY, timestamp);

        if (contentClipOptional.isPresent()) {
            graphics.setClip(null);
        }

        if (!template.isBackground()) {
            graphics.drawImage(image, 0, 0, null);
        }

        graphics.dispose();
        return imageWithText;
    }

    public static BufferedImage cutoutImage(BufferedImage imageToCut, BufferedImage imageToCutout, int x, int y, int cutoutColor) {
        imageToCut = ImageUtil.convertType(imageToCut, BufferedImage.TYPE_INT_ARGB);

        var toCutWidth = imageToCut.getWidth();
        var toCutHeight = imageToCut.getHeight();

        var toCutoutWidth = imageToCutout.getWidth();
        var toCutoutHeight = imageToCutout.getHeight();

        var toCutPixels = imageToCut.getRGB(0, 0, toCutWidth, toCutHeight, null, 0, toCutWidth);
        var toCutoutPixels = imageToCutout.getRGB(0, 0, toCutoutWidth, toCutoutHeight, null, 0, toCutoutWidth);

        for (var i = 0; i < toCutoutPixels.length; i++) {
            var toCutoutRgb = toCutoutPixels[i];
            if (!isTransparent(toCutoutRgb)) {
                var toCutIndex = get1dIndex(Math.min(toCutWidth, x + getX(i, toCutWidth)), Math.min(toCutHeight, y + getY(i, toCutWidth)), toCutWidth);
                if (toCutIndex < toCutPixels.length) {
                    toCutPixels[toCutIndex] = cutoutColor;
                }
            }
        }
        imageToCut.setRGB(0, 0, toCutWidth, toCutHeight, toCutPixels, 0, toCutWidth);
        return imageToCut;
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

    public static BufferedImage rotate(
        BufferedImage image,
        double radians,
        @Nullable Integer newWidth,
        @Nullable Integer newHeight,
        @Nullable Color backgroundColor,
        int resultType
    ) {
        var sin = Math.abs(Math.sin(radians));
        var cos = Math.abs(Math.cos(radians));

        var width = image.getWidth();
        var height = image.getHeight();

        if (newWidth == null) {
            newWidth = (int) Math.floor(width * cos + height * sin);
        }

        if (newHeight == null) {
            newHeight = (int) Math.floor(height * cos + width * sin);
        }

        var rotated = new BufferedImage(newWidth, newHeight, resultType);
        var graphics = rotated.createGraphics();

        if (backgroundColor != null) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, newWidth, newHeight);
        }

        graphics.translate((newWidth - width) / 2, (newHeight - height) / 2);
        graphics.rotate(radians, width / 2F, height / 2F);
        graphics.drawRenderedImage(image, null);
        graphics.dispose();

        return rotated;
    }

    public static String getImageFormat(InputStream inputStream) throws IOException {
        try (var imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            if (imageInputStream != null) {
                var imageReaders = ImageIO.getImageReaders(imageInputStream);
                if (imageReaders.hasNext()) {
                    var reader = imageReaders.next();
                    return reader.getFormatName().toLowerCase();
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

    public static BufferedImage convertType(BufferedImage image, int type) {
        if (image.getType() == type) {
            return image;
        } else {
            BufferedImage newType = new BufferedImage(image.getWidth(), image.getHeight(), type);
            ColorConvertOp convertOp = new ColorConvertOp(null);
            return convertOp.filter(image, newType);
        }
    }

    public static int getType(BufferedImage image) {
        int type = image.getType();
        return type < BufferedImage.TYPE_INT_RGB || type > BufferedImage.TYPE_BYTE_INDEXED
            ? BufferedImage.TYPE_INT_ARGB
            : type;
    }

    public static Area getArea(BufferedImage image) {
        GeneralPath path = new GeneralPath();
        boolean continue_ = false;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if (isTransparent(image.getRGB(x, y))) {
                    continue_ = false;
                } else {
                    if (continue_) {
                        path.lineTo(x, y);
                        path.lineTo(x, y + 1);
                        path.lineTo(x + 1, y + 1);
                        path.lineTo(x + 1, y);
                        path.lineTo(x, y);
                    } else {
                        path.moveTo(x, y);
                    }
                    continue_ = true;
                }
            }
            continue_ = false;
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
        int tolerance = 40;
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        return Math.abs(red - green) <= tolerance && Math.abs(red - blue) <= tolerance && Math.abs(green - blue) <= tolerance;
    }

    public static BufferedImage copy(BufferedImage image) {
        var copy = copySize(image);
        var graphics = copy.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    public static BufferedImage copySize(BufferedImage image) {
        return new BufferedImage(image.getWidth(), image.getHeight(), getType(image));
    }

    public static int hashImage(BufferedImage image) {
        if (image == null) {
            return 0;
        }
        var hash = 1;
        for (var x = 0; x < image.getWidth(); x++) {
            for (var y = 0; y < image.getHeight(); y++) {
                hash = 31 * hash + image.getRGB(x, y);
            }
        }
        return hash;
    }

    public static boolean imageEquals(BufferedImage image1, BufferedImage image2) {
        if (Objects.equals(image1, image2)) {
            return true;
        }
        if (image1 == null || image2 == null) {
            return false;
        }
        if (image1.getWidth() != image2.getWidth()) {
            return false;
        }
        if (image1.getHeight() != image2.getHeight()) {
            return false;
        }
        for (var x = 0; x < image1.getWidth(); x++) {
            for (var y = 0; y < image1.getHeight(); y++) {
                if (image1.getRGB(x, y) != image2.getRGB(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BufferedImage makeRoundedCorners(BufferedImage image, float cornerRadius) {
        var width = image.getWidth();
        var height = image.getHeight();
        var output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = output.createGraphics();

        /*
        This is what we want, but it only does hard-clipping, i.e. aliasing
        graphics.setClip(new RoundRectangle2D ...)
        So instead fake soft-clipping by first drawing the desired clip shape
        in fully opaque white with antialiasing enabled...
         */
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(Color.WHITE);
        graphics.fill(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        /*
        ...then compositing the image on top,
        using the white shape from above as alpha source
         */
        graphics.setComposite(AlphaComposite.SrcAtop);
        graphics.drawImage(image, 0, 0, null);

        graphics.dispose();
        return output;
    }

    public static BufferedImage flipX(BufferedImage image) {
        return ImmutableImage.wrapAwt(image).flipX().awt();
    }

    public static BufferedImage flipY(BufferedImage image) {
        return ImmutableImage.wrapAwt(image).flipY().awt();
    }
}
