package io.github.shaksternano.mediamanipulator.util;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

/**
 * Contains static methods for dealing with images.
 */
public class ImageUtil {

    /**
     * Adds a caption to an image.
     *
     * @param image   The image to add a caption to.
     * @param caption The caption to add.
     * @param font    The font to use for the caption.
     * @return The image with the caption added.
     */
    public static BufferedImage captionImage(BufferedImage image, String caption, Font font) {
        font = font.deriveFont(image.getHeight() / 10F);
        int padding = (int) (image.getHeight() * 0.05);
        Graphics2D graphics = image.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        graphics.setFont(font);
        int fillHeight = calculateCaptionBoxHeight(caption, graphics, padding, padding, image.getWidth());
        graphics.dispose();

        BufferedImage resizedImage = new BufferedImage(image.getWidth(), image.getHeight() + fillHeight, image.getType());

        graphics = resizedImage.createGraphics();
        graphics.setFont(font);

        graphics.drawImage(image, 0, fillHeight, null);

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

        graphics.setColor(Color.BLACK);

        drawText(caption, graphics, padding, padding, resizedImage.getWidth());

        graphics.dispose();
        return resizedImage;
    }

    /**
     * Calculates the height of the caption box.
     *
     * @param text       The text of the caption.
     * @param graphics   The {@link Graphics2D} of the image being captioned.
     * @param paddingX   The horizontal padding of each side of the caption box.
     * @param paddingY   The vertical padding of each side of the caption box.
     * @param imageWidth The width of the image being captioned.
     * @return The height of the caption box.
     */
    private static int calculateCaptionBoxHeight(String text, Graphics2D graphics, int paddingX, int paddingY, float imageWidth) {
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());

        AttributedCharacterIterator paragraph = attributedString.getIterator();

        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();

        FontRenderContext renderContext = graphics.getFontRenderContext();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, renderContext);

        lineMeasurer.setPosition(paragraphStart);

        float maxLineWidth = imageWidth - paddingX * 2;
        int height = paddingY * 2;

        while (lineMeasurer.getPosition() < paragraphEnd) {
            TextLayout layout = lineMeasurer.nextLayout(maxLineWidth);
            height += layout.getAscent() + layout.getDescent() + layout.getLeading();
        }

        return height;
    }

    /**
     * Draws the text of the caption.
     *
     * @param text       The text of the caption.
     * @param graphics   The {@link Graphics2D} of the image being captioned.
     * @param paddingX   The horizontal padding of each side of the caption box.
     * @param paddingY   The vertical padding of each side of the caption box.
     * @param imageWidth The width of the image being captioned.
     */
    private static void drawText(String text, Graphics2D graphics, int paddingX, int paddingY, int imageWidth) {
        AttributedString attributedString = new AttributedString(text);
        attributedString.addAttribute(TextAttribute.FONT, graphics.getFont());

        AttributedCharacterIterator paragraph = attributedString.getIterator();

        int paragraphStart = paragraph.getBeginIndex();
        int paragraphEnd = paragraph.getEndIndex();

        FontRenderContext renderContext = graphics.getFontRenderContext();
        LineBreakMeasurer lineMeasurer = new LineBreakMeasurer(paragraph, renderContext);

        lineMeasurer.setPosition(paragraphStart);

        float maxLineWidth = imageWidth - paddingX * 2;
        int y = paddingY;

        while (lineMeasurer.getPosition() < paragraphEnd) {
            TextLayout layout = lineMeasurer.nextLayout(maxLineWidth);
            int x = (int) (paddingX + ((maxLineWidth - layout.getAdvance()) / 2));
            y += layout.getAscent();
            layout.draw(graphics, x, y);
            y += layout.getDescent() + layout.getLeading();
        }
    }

    /**
     * Stretches an image.
     *
     * @param image        The image to stretch.
     * @param targetWidth  The width to stretch the image to.
     * @param targetHeight The height to stretch the image to.
     * @return The stretched image.
     */
    public static BufferedImage stretch(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage stretchedImage = new BufferedImage(targetWidth, targetHeight, image.getType());
        Graphics2D graphics = stretchedImage.createGraphics();
        graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();
        return stretchedImage;
    }

    /**
     * Overlays an image on top of another image.
     *
     * @param image       The image being overlaid on.
     * @param overlay     The image to overlay.
     * @param x           The x coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param y           The y coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param expand      Whether to expand the resulting media to fit the overlay file.
     * @param expandColor The background color used if the resulting media is expanded.
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
            }

            graphics.fillRect(0, 0, overlaidImage.getWidth(), overlaidImage.getHeight());

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
}
