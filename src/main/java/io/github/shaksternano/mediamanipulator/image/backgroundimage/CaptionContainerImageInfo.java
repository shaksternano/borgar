package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

public class CaptionContainerImageInfo implements ContainerImageInfo {

    private final ImageMedia IMAGE;
    private final String RESULT_NAME;
    private final int CONTENT_X;
    private final int CONTENT_Y;
    private final int CONTENT_WIDTH;
    private final int CONTENT_HEIGHT;
    private final boolean IS_BACKGROUND;
    @Nullable
    private final Color FILL;
    private final Font FONT;
    private final Color TEXT_COLOR;

    public CaptionContainerImageInfo(
            ImageMedia image,
            String resultName,
            int contentContainerX,
            int contentContainerY,
            int contentContainerWidth,
            int contentContainerHeight,
            int contentContainerPadding,
            boolean isBackground,
            @Nullable Color fill,
            Font font,
            Color textColor
    ) {
        IMAGE = image;
        RESULT_NAME = resultName;
        CONTENT_X = contentContainerX + contentContainerPadding;
        CONTENT_Y = contentContainerY + contentContainerPadding;
        int doublePadding = contentContainerPadding * 2;
        CONTENT_WIDTH = contentContainerWidth - doublePadding;
        CONTENT_HEIGHT = contentContainerHeight - doublePadding;
        IS_BACKGROUND = isBackground;
        FILL = fill;
        FONT = font;
        TEXT_COLOR = textColor;
    }

    @Override
    public ImageMedia getImage() {
        return IMAGE;
    }

    @Override
    public String getResultName() {
        return RESULT_NAME;
    }

    @Override
    public int getImageContentX() {
        return CONTENT_X;
    }

    @Override
    public int getImageContentY() {
        return CONTENT_Y;
    }

    @Override
    public int getImageContentWidth() {
        return CONTENT_WIDTH;
    }

    @Override
    public int getImageContentHeight() {
        return CONTENT_HEIGHT;
    }

    @Override
    public int getTextContentX() {
        return CONTENT_X;
    }

    @Override
    public int getTextContentY() {
        return CONTENT_Y;
    }

    @Override
    public int getTextContentWidth() {
        return CONTENT_WIDTH;
    }

    @Override
    public int getTextContentHeight() {
        return CONTENT_HEIGHT;
    }

    @Override
    public boolean isBackground() {
        return IS_BACKGROUND;
    }

    @Override
    public Optional<Color> getFill() {
        return Optional.ofNullable(FILL);
    }

    @Override
    public Font getFont() {
        return FONT;
    }

    @Override
    public Color getTextColor() {
        return TEXT_COLOR;
    }
}
