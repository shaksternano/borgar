package io.github.shaksternano.mediamanipulator.graphics.drawable.util;

import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;

import java.awt.*;

public class DrawableUtil {

    public static int fitHeight(int maxHeight, Drawable drawable, Graphics2D graphics) {
        Font font = graphics.getFont();
        int paragraphHeight = drawable.getHeight(graphics);
        while (paragraphHeight > maxHeight) {
            float sizeRatio = (float) paragraphHeight / maxHeight;
            font = font.deriveFont(font.getSize() - sizeRatio);
            graphics.setFont(font);
            paragraphHeight = drawable.getHeight(graphics);
        }

        return paragraphHeight;
    }
}
