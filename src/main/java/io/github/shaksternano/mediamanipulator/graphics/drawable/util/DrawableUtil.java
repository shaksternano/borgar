package io.github.shaksternano.mediamanipulator.graphics.drawable.util;

import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;

import java.awt.*;

public class DrawableUtil {

    public static int fitHeight(int maxHeight, Drawable text, Graphics2D graphics) {
        Font font = graphics.getFont();
        int textHeight = text.getHeight(graphics);
        while (textHeight > maxHeight) {
            float sizeRatio = (float) textHeight / maxHeight;
            font = font.deriveFont(font.getSize() - sizeRatio);
            graphics.setFont(font);
            textHeight = text.getHeight(graphics);
        }

        return textHeight;
    }
}
