package io.github.shaksternano.mediamanipulator.graphics.drawable;

import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class ParagraphCompositeDrawable extends ListCompositeDrawable {

    private final TextAlignment ALIGNMENT;
    private final int MAX_WIDTH;
    @Nullable
    private final Integer MAX_HEIGHT;

    private static final Drawable SPACE = new TextDrawable(" ");

    public ParagraphCompositeDrawable(TextAlignment alignment, int maxWidth, @Nullable Integer maxHeight) {
        ALIGNMENT = alignment;
        MAX_WIDTH = Math.max(0, maxWidth);
        MAX_HEIGHT = maxHeight == null ? null : Math.max(0, maxHeight);
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y) {
        Font font = graphics.getFont();
        boolean needToResetFont = false;

        if (MAX_HEIGHT != null) {
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
        int lineX;
        int lineY = y;

        List<Drawable> currentLine = new ArrayList<>();

        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphics);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphics);
            }
            if (newLineWidth <= MAX_WIDTH) {
                currentLine.add(part);
                lineWidth = newLineWidth;
            } else {
                lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);

                int spaceWidth = SPACE.getWidth(graphics);
                if (ALIGNMENT == TextAlignment.JUSTIFY) {
                    spaceWidth += (MAX_WIDTH - lineWidth) / (currentLine.size() - 1);
                }

                for (Drawable linePart : currentLine) {
                    linePart.draw(graphics, lineX, lineY);
                    lineX += linePart.getWidth(graphics) + spaceWidth;
                }

                currentLine.clear();
                currentLine.add(part);
                lineWidth = part.getWidth(graphics);
                lineY += lineHeight + lineSpace;
            }
        }

        lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);
        for (Drawable linePart : currentLine) {
            linePart.draw(graphics, lineX, lineY);
            lineX += linePart.getWidth(graphics) + SPACE.getWidth(graphics);
        }

        if (needToResetFont) {
            graphics.setFont(font);
        }
    }

    private static int calculateTextXPosition(TextAlignment alignment, int x, int lineWidth, int maxWidth) {
        switch (alignment) {
            case CENTER -> x += (maxWidth - lineWidth) / 2;
            case RIGHT -> x += maxWidth - lineWidth;
        }

        return x;
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineWidth = 0;

        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphicsContext);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphicsContext);
            }
            if (newLineWidth <= MAX_WIDTH) {
                lineWidth = newLineWidth;
            } else {
                lineWidth = part.getWidth(graphicsContext);
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

        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int newLineWidth = lineWidth + part.getWidth(graphicsContext);
            if (lineWidth > 0) {
                newLineWidth += SPACE.getWidth(graphicsContext);
            }
            if (newLineWidth <= MAX_WIDTH) {
                lineWidth = newLineWidth;
            } else {
                lineWidth = part.getWidth(graphicsContext);
                lineY += lineHeight + lineSpace;
            }
        }

        lineY += lineHeight;
        return lineY;
    }

    @Override
    public Drawable resizeToWidth(int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Drawable resizeToHeight(int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ALIGNMENT, MAX_WIDTH, MAX_HEIGHT);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParagraphCompositeDrawable other) {
            return Objects.equals(getParts(), other.getParts()) &&
                    Objects.equals(ALIGNMENT, other.ALIGNMENT) &&
                    MAX_WIDTH == other.MAX_WIDTH &&
                    Objects.equals(MAX_HEIGHT, other.MAX_HEIGHT);
        } else {
            return false;
        }
    }

    public static class Builder {

        private final List<Drawable> words = new ArrayList<>();

        private final Map<String, Drawable> NON_TEXT_PARTS = new TreeMap<>(Comparator
                .comparingInt(String::length)
                .reversed()
                .thenComparing(Comparator.naturalOrder())
        );

        public Builder(Map<String, Drawable> nonTextParts) {
            NON_TEXT_PARTS.putAll(nonTextParts);
        }

        public Builder addWords(String... words) {
            for (String word : words) {
                addCompositeWord(word);
            }

            return this;
        }

        public CompositeDrawable build(TextAlignment alignment, int maxWidth, @Nullable Integer maxHeight) {
            CompositeDrawable paragraph = new ParagraphCompositeDrawable(alignment, maxWidth, maxHeight);

            for (Drawable part : words) {
                paragraph.addPart(part);
            }

            return paragraph;
        }

        private void addCompositeWord(String word) {
            if (NON_TEXT_PARTS.isEmpty()) {
                words.add(new TextDrawable(word));
            } else {
                CompositeDrawable compositeWord = new HorizontalCompositeDrawable();
                StringBuilder actualWordBuilder = new StringBuilder();

                int index = 0;
                while (index < word.length()) {
                    String subWord = word.substring(index);
                    boolean foundImage = false;

                    for (Map.Entry<String, Drawable> entry : NON_TEXT_PARTS.entrySet()) {
                        String key = entry.getKey();
                        Drawable part = entry.getValue();
                        int keyLength = key.length();
                        if (subWord.startsWith(key)) {
                            if (!actualWordBuilder.isEmpty()) {
                                compositeWord.addPart(new TextDrawable(actualWordBuilder.toString()));
                                actualWordBuilder.setLength(0);
                            }

                            compositeWord.addPart(part);
                            index += keyLength;
                            foundImage = true;
                            break;
                        }
                    }

                    if (!foundImage) {
                        actualWordBuilder.append(subWord.charAt(0));
                        index++;
                    }
                }

                if (!actualWordBuilder.isEmpty()) {
                    compositeWord.addPart(new TextDrawable(actualWordBuilder.toString()));
                }

                words.add(compositeWord);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(words, NON_TEXT_PARTS);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Builder other) {
                return Objects.equals(words, other.words) &&
                        Objects.equals(NON_TEXT_PARTS, other.NON_TEXT_PARTS);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[Words: " + words + ", Non-text parts: " + NON_TEXT_PARTS + "]";
        }
    }
}
