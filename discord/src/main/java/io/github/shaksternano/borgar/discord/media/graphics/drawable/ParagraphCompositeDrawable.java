package io.github.shaksternano.borgar.discord.media.graphics.drawable;

import io.github.shaksternano.borgar.discord.media.graphics.TextAlignment;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class ParagraphCompositeDrawable extends ListCompositeDrawable {

    private final TextAlignment ALIGNMENT;
    private final int MAX_WIDTH;

    private static final Drawable SPACE = new TextDrawable(" ");

    public ParagraphCompositeDrawable(TextAlignment alignment, int maxWidth) {
        ALIGNMENT = alignment;
        MAX_WIDTH = Math.max(0, maxWidth);
    }

    @Override
    public void draw(Graphics2D graphics, int x, int y, long timestamp) throws IOException {
        FontMetrics metrics = graphics.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineX;
        int lineY = y;

        List<Drawable> currentLine = new ArrayList<>();
        for (int i = 0; i < getParts().size(); i++) {
            Drawable part = getParts().get(i);
            try {
                part = part.resizeToHeight(lineHeight);
                getParts().set(i, part);
            } catch (UnsupportedOperationException ignored) {
            }

            int partWidth = part.getWidth(graphics);
            int spaceWidth = SPACE.getWidth(graphics);
            int newLineWidth = lineWidth + partWidth;
            if (lineWidth > 0) {
                newLineWidth += spaceWidth;
            }

            if (newLineWidth <= MAX_WIDTH || currentLine.isEmpty()) {
                currentLine.add(part);
                lineWidth = newLineWidth;
            } else {
                lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);

                if (ALIGNMENT == TextAlignment.JUSTIFY) {
                    spaceWidth += (MAX_WIDTH - lineWidth) / (currentLine.size() - 1);
                }

                for (Drawable linePart : currentLine) {
                    linePart.draw(graphics, lineX, lineY, timestamp);
                    lineX += linePart.getWidth(graphics) + spaceWidth;
                }

                currentLine.clear();
                currentLine.add(part);
                lineWidth = partWidth;
                lineY += lineHeight + lineSpace;
            }
        }

        lineX = calculateTextXPosition(ALIGNMENT, x, lineWidth, MAX_WIDTH);
        for (Drawable linePart : currentLine) {
            linePart.draw(graphics, lineX, lineY, timestamp);
            lineX += linePart.getWidth(graphics) + SPACE.getWidth(graphics);
        }
    }

    private static int calculateTextXPosition(TextAlignment alignment, int x, int lineWidth, int maxWidth) {
        switch (alignment) {
            case CENTRE -> x += (maxWidth - lineWidth) / 2;
            case RIGHT -> x += maxWidth - lineWidth;
        }

        return x;
    }

    @Override
    public int getWidth(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineWidth = 0;
        int maxLineWidth = 0;

        boolean currentLineIsEmpty = true;
        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int partWidth = part.getWidth(graphicsContext);
            int spaceWidth = SPACE.getWidth(graphicsContext);
            int newLineWidth = lineWidth + partWidth;
            if (lineWidth > 0) {
                newLineWidth += spaceWidth;
            }

            if (newLineWidth <= MAX_WIDTH || currentLineIsEmpty) {
                lineWidth = newLineWidth;
                currentLineIsEmpty = false;
            } else {
                lineWidth = partWidth;
                currentLineIsEmpty = true;
            }

            maxLineWidth = Math.max(maxLineWidth, lineWidth);
        }

        return maxLineWidth;
    }

    @Override
    public int getHeight(Graphics2D graphicsContext) {
        FontMetrics metrics = graphicsContext.getFontMetrics();
        int lineHeight = metrics.getAscent() + metrics.getDescent();
        int lineSpace = metrics.getLeading();
        int lineWidth = 0;
        int lineY = 0;

        boolean currentLineIsEmpty = true;
        for (Drawable part : getParts()) {
            try {
                part = part.resizeToHeight(lineHeight);
            } catch (UnsupportedOperationException ignored) {
            }

            int partWidth = part.getWidth(graphicsContext);
            int spaceWidth = SPACE.getWidth(graphicsContext);
            int newLineWidth = lineWidth + partWidth;
            if (lineWidth > 0) {
                newLineWidth += spaceWidth;
            }

            if (newLineWidth <= MAX_WIDTH || currentLineIsEmpty) {
                lineWidth = newLineWidth;
                currentLineIsEmpty = false;
            } else {
                lineWidth = partWidth;
                lineY += lineHeight + lineSpace;
                currentLineIsEmpty = true;
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
        return Objects.hash(super.hashCode(), ALIGNMENT, MAX_WIDTH);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParagraphCompositeDrawable other) {
            return Objects.equals(getParts(), other.getParts())
                && Objects.equals(ALIGNMENT, other.ALIGNMENT)
                && MAX_WIDTH == other.MAX_WIDTH;
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

        public Builder addWords(@Nullable Function<String, Drawable> customTextDrawableFactory, Iterable<String> words) {
            for (String word : words) {
                addWord(customTextDrawableFactory, word);
            }

            return this;
        }

        public Builder addWord(@Nullable Function<String, Drawable> customTextDrawableFactory, String word) {
            if (NON_TEXT_PARTS.isEmpty()) {
                Drawable textPart = customTextDrawableFactory == null ?
                    new TextDrawable(word) :
                    customTextDrawableFactory.apply(word);
                words.add(textPart);
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
                                String text = actualWordBuilder.toString();
                                Drawable textPart = customTextDrawableFactory == null ?
                                    new TextDrawable(text) :
                                    customTextDrawableFactory.apply(text);
                                compositeWord.addPart(textPart);
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
                    String text = actualWordBuilder.toString();
                    Drawable textPart = customTextDrawableFactory == null ?
                        new TextDrawable(text) :
                        customTextDrawableFactory.apply(text);
                    compositeWord.addPart(textPart);
                }

                words.add(compositeWord);
            }

            return this;
        }

        public ParagraphCompositeDrawable build(TextAlignment alignment, int maxWidth) {
            ParagraphCompositeDrawable paragraph = new ParagraphCompositeDrawable(alignment, maxWidth);

            for (Drawable part : words) {
                paragraph.addPart(part);
            }

            return paragraph;
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
                return Objects.equals(words, other.words)
                    && Objects.equals(NON_TEXT_PARTS, other.NON_TEXT_PARTS);
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
