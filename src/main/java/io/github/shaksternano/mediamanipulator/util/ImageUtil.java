package io.github.shaksternano.mediamanipulator.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class ImageUtil {

    public static BufferedImage captionImage(BufferedImage image, String caption, Font font) {
        int fillHeight = image.getHeight() / 4;

        BufferedImage resizedImage = new BufferedImage(image.getWidth(), image.getHeight() + fillHeight, image.getType());

        Graphics2D graphics = resizedImage.createGraphics();

        graphics.drawImage(image, 0, fillHeight, null);

        graphics.setRenderingHint(
                RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB
        );

        graphics.setFont(font.deriveFont((float) fillHeight / 2));

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, resizedImage.getWidth(), fillHeight);

        graphics.setColor(Color.BLACK);

        FontMetrics metrics = graphics.getFontMetrics();
        int textX = (resizedImage.getWidth() - metrics.stringWidth(caption)) / 2;
        int textY = ((fillHeight - metrics.getHeight()) / 2) + metrics.getAscent();

        graphics.drawString(caption, textX, textY);
        graphics.dispose();

        return resizedImage;
    }
}
