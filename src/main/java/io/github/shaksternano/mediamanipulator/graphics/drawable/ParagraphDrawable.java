package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ParagraphDrawable extends BaseCompositeDrawable {

    private final TextAlignment ALIGNMENT;
    private final int MAX_WIDTH;
    private final int MAX_HEIGHT;

    private static final Drawable SPACE = new TextDrawable(" ");

    public ParagraphDrawable(TextAlignment alignment, int maxWidth, int maxHeight) {
        ALIGNMENT = alignment;
        MAX_WIDTH = Math.max(0, maxWidth);
        MAX_HEIGHT = Math.max(0, maxHeight);
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        Font font = graphics.getFont();
        boolean needToResetFont = false;

        if (MAX_HEIGHT > 0) {
            float sizeRatio = (float) MAX_HEIGHT / getHeight(graphics);
            if (sizeRatio < 1) {
                float newSize = font.getSize() * sizeRatio;
                graphics.setFont(font.deriveFont(newSize));
                needToResetFont = true;
            }
        }

        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineX = x;
        int lineY = y;

        if (MAX_WIDTH > 0) {
            List<Drawable> line = new ArrayList<>();

            Iterator<Drawable> drawableIterator = getParts().iterator();
            while (drawableIterator.hasNext()) {
                Drawable part = drawableIterator.next();

                if (part instanceof ResizableDrawable resizablePart) {
                    resizablePart.resizeToHeight(lineHeight);
                }

                lineWidth += part.getWidth(graphics);
                if (lineWidth <= MAX_WIDTH) {
                    line.add(part);
                }

                if (lineWidth > MAX_WIDTH || !drawableIterator.hasNext()) {
                    lineX = x;
                    switch (ALIGNMENT) {
                        case CENTER -> lineX += (MAX_WIDTH - lineWidth) / 2;
                        case RIGHT -> lineX += MAX_WIDTH - lineWidth;
                    }

                    for (Drawable linePart : line) {
                        linePart.draw(graphics, lineX, lineY);
                        lineX += linePart.getWidth(graphics) + SPACE.getWidth(graphics);
                    }
                }

                if (lineWidth > MAX_WIDTH) {
                    line.clear();
                    line.add(part);
                    lineWidth = part.getWidth(graphics);
                    lineY += lineHeight + lineSpace;
                }

                lineWidth += SPACE.getWidth(graphics);
            }
        } else {
            for (Drawable part : getParts()) {
                part.draw(graphics, lineX, lineY);
                lineX += part.getWidth(graphics) + SPACE.getWidth(graphics);
            }
        }

        if (needToResetFont) {
            graphics.setFont(font);
        }
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineWidth = 0;

        if (MAX_WIDTH > 0) {
            for (Drawable part : getParts()) {
                if (part instanceof ResizableDrawable resizablePart) {
                    resizablePart.resizeToHeight(lineHeight);
                }

                lineWidth += part.getWidth(graphicsContext);

                if (lineWidth >= MAX_WIDTH) {
                    return MAX_WIDTH;
                }

                lineWidth += SPACE.getWidth(graphicsContext);
            }
        } else {
            for (Drawable part : getParts()) {
                lineWidth += part.getWidth(graphicsContext) + SPACE.getWidth(graphicsContext);
            }
        }

        return lineWidth;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineY = 0;

        if (MAX_WIDTH > 0) {
            for (Drawable part : getParts()) {
                if (part instanceof ResizableDrawable resizablePart) {
                    resizablePart.resizeToHeight(lineHeight);
                }

                lineWidth += part.getWidth(graphicsContext);

                if (lineWidth >= MAX_WIDTH) {
                    lineWidth = part.getWidth(graphicsContext);
                    lineY += lineHeight;

                    if (MAX_HEIGHT > 0 && lineY >= MAX_HEIGHT) {
                        return MAX_HEIGHT;
                    }
                }

                lineWidth += SPACE.getWidth(graphicsContext);
                lineY += lineSpace;
            }
        }

        lineY += lineHeight;
        return MAX_HEIGHT <= 0 ? lineY : Math.min(lineY, MAX_HEIGHT);
    }
}
