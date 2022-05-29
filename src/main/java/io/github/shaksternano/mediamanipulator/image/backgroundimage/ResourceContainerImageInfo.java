package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.util.Fonts;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public enum ResourceContainerImageInfo implements ContainerImageInfo {

    SONIC_SAYS(
            "image/containerimage/sonic_says.jpg",
            "sonic_says",
            345,
            35,
            630,
            490,
            60,
            true,
            null,
            Fonts.getCustomFont("bitstream_vera_sans"),
            Color.WHITE,
            100
    ),

    SOYJAK_POINTING(
            "image/containerimage/soyjak_pointing.png",
            "soyjak_pointing",
            200,
            100,
            600,
            400,
            0,
            false,
            Color.WHITE,
            Fonts.getCustomFont("futura_condensed_extra_bold"),
            Color.BLACK,
            100
    );

    private final String IMAGE_PATH;
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

    ResourceContainerImageInfo(
            String imagePath,
            String resultName,
            int contentContainerX,
            int contentContainerY,
            int contentContainerWidth,
            int contentContainerHeight,
            int contentContainerPadding,
            boolean isBackground,
            @Nullable Color fill,
            Font font,
            Color textColor,
            float maxFontSize
    ) {
        IMAGE_PATH = imagePath;
        RESULT_NAME = resultName;
        CONTENT_X = contentContainerX + contentContainerPadding;
        CONTENT_Y = contentContainerY + contentContainerPadding;
        int doublePadding = contentContainerPadding * 2;
        CONTENT_WIDTH = contentContainerWidth - doublePadding;
        CONTENT_HEIGHT = contentContainerHeight - doublePadding;
        IS_BACKGROUND = isBackground;
        FILL = fill;
        FONT = font.deriveFont(maxFontSize);
        TEXT_COLOR = textColor;
    }

    @Override
    public ImageMedia getImage() throws IOException {
        return ImageUtil.getImageResource(IMAGE_PATH);
    }

    @Override
    public String getResultName() {
        return RESULT_NAME;
    }

    @Override
    public int getContentX() {
        return CONTENT_X;
    }

    @Override
    public int getContentY() {
        return CONTENT_Y;
    }

    @Override
    public int getContentWidth() {
        return CONTENT_WIDTH;
    }

    @Override
    public int getContentHeight() {
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

    public static void validateFilePaths() {
        for (ResourceContainerImageInfo backgroundImage : ResourceContainerImageInfo.values()) {
            try {
                FileUtil.validateResourcePath(backgroundImage.IMAGE_PATH);
            } catch (Throwable t) {
                Main.getLogger().error("Error with " + backgroundImage + "'s file path " + backgroundImage.IMAGE_PATH, t);
                Main.shutdown(1);
            }
        }
    }
}
