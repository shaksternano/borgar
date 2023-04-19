package io.github.shaksternano.mediamanipulator.media.graphics;

import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.media.graphics.drawable.Drawable;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;

public class GraphicsUtil {

    public static int fontFitWidth(int maxWidth, Drawable text, Graphics2D graphics) {
        Font font = graphics.getFont();
        int textWidth = text.getWidth(graphics);
        while (textWidth > maxWidth) {
            float sizeRatio = (float) textWidth / maxWidth;
            font = font.deriveFont(font.getSize() - sizeRatio);
            graphics.setFont(font);
            textWidth = text.getWidth(graphics);
        }

        return textWidth;
    }

    public static int fontFitHeight(int maxHeight, Drawable text, Graphics2D graphics) {
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

    public static Shape loadShape(String resourcePath) throws IOException {
        try (ObjectInputStream input = new ObjectInputStream(FileUtil.getResourceInRootPackage(resourcePath))) {
            Object parsedObject = input.readObject();
            if (parsedObject instanceof Shape shape) {
                return shape;
            } else {
                throw new IOException("Failed to load shape file under \"" + resourcePath + "\"! The parsed object is not a shape!");
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to load shape file under \"" + resourcePath + "\"!", e);
        }
    }
}
