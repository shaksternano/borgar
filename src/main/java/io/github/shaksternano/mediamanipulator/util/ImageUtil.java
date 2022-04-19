package io.github.shaksternano.mediamanipulator.util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public class ImageUtil {

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

    public static BufferedImage stretch(BufferedImage image, float widthMultiplier, float heightMultiplier) {
        int newWidth = (int) (image.getWidth() * widthMultiplier);
        int newHeight = (int) (image.getHeight() * heightMultiplier);
        BufferedImage stretchedImage = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D graphics = stretchedImage.createGraphics();
        graphics.drawImage(image, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        return stretchedImage;
    }
}
