package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.graphics.GraphicsUtil;
import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.image.util.ImageUtil;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public enum ResourceContainerImageInfo implements ContainerImageInfo {

    SONIC_SAYS(
            "image/containerimage/sonic_says.png",
            "sonic_says",
            210,
            15,
            410,
            315,
            40,
            Position.CENTRE,
            null,
            true,
            null,
            "Bitstream Vera Sans",
            Color.WHITE,
            70,
            null
    ),

    SOYJAK_POINTING(
            "image/containerimage/soyjak_pointing.png",
            "soyjak_pointing",
            0,
            0,
            1024,
            810,
            0,
            Position.CENTRE,
            250,
            150,
            500,
            300,
            0,
            Position.CENTRE,
            null,
            false,
            null,
            "Futura-CondensedExtraBold",
            Color.BLACK,
            100,
            null
    ),

    THINKING_BUBBLE(
            "image/containerimage/thinking_bubble.png",
            "thinking_bubble",
            12,
            0,
            116,
            81,
            10,
            Position.CENTRE,
            12,
            0,
            116,
            81,
            20,
            Position.CENTRE,
            "shape/thinking_bubble_edge_trimmed.javaobject",
            false,
            Color.WHITE,
            "Futura-CondensedExtraBold",
            Color.BLACK,
            25,
            null
    );

    private final String IMAGE_PATH;
    private final String RESULT_NAME;
    private final int IMAGE_CONTENT_X;
    private final int IMAGE_CONTENT_Y;
    private final int IMAGE_CONTENT_WIDTH;
    private final int IMAGE_CONTENT_HEIGHT;
    private final Position IMAGE_CONTENT_POSITION;
    private final int TEXT_CONTENT_X;
    private final int TEXT_CONTENT_Y;
    private final int TEXT_CONTENT_WIDTH;
    private final int TEXT_CONTENT_HEIGHT;
    private final Position TEXT_CONTENT_POSITION;
    @Nullable
    private final String CONTENT_CLIP_SHAPE_FILE_PATH;
    private final boolean IS_BACKGROUND;
    @Nullable
    private final Color FILL;
    private final String FONT_NAME;
    private final Font FONT;
    private final Color TEXT_COLOR;
    @Nullable
    private final Function<String, Drawable> CUSTOM_TEXT_DRAWABLE_FACTORY;

    ResourceContainerImageInfo(
            String imagePath,
            String resultName,
            int imageContainerX,
            int imageContainerY,
            int imageContainerWidth,
            int imageContainerHeight,
            int imageContainerPadding,
            Position imageContentPosition,
            int textContainerX,
            int textContainerY,
            int textContainerWidth,
            int textContainerHeight,
            int textContainerPadding,
            Position textContentPosition,
            @Nullable String contentClipShapeFilePath,
            boolean isBackground,
            @Nullable Color fill,
            String fontName,
            Color textColor,
            int maxFontSize,
            @Nullable Function<String, Drawable> customTextDrawableFactory
    ) {
        IMAGE_PATH = imagePath;
        RESULT_NAME = resultName;
        IMAGE_CONTENT_X = imageContainerX + imageContainerPadding;
        IMAGE_CONTENT_Y = imageContainerY + imageContainerPadding;
        TEXT_CONTENT_X = textContainerX + textContainerPadding;
        TEXT_CONTENT_Y = textContainerY + textContainerPadding;
        int doubleImagePadding = imageContainerPadding * 2;
        int doubleTextPadding = textContainerPadding * 2;
        IMAGE_CONTENT_WIDTH = imageContainerWidth - doubleImagePadding;
        IMAGE_CONTENT_HEIGHT = imageContainerHeight - doubleImagePadding;
        IMAGE_CONTENT_POSITION = imageContentPosition;
        TEXT_CONTENT_WIDTH = textContainerWidth - doubleTextPadding;
        TEXT_CONTENT_HEIGHT = textContainerHeight - doubleTextPadding;
        TEXT_CONTENT_POSITION = textContentPosition;
        CONTENT_CLIP_SHAPE_FILE_PATH = contentClipShapeFilePath;
        IS_BACKGROUND = isBackground;
        FILL = fill;
        FONT_NAME = fontName;
        FONT = new Font(FONT_NAME, Font.PLAIN, maxFontSize);
        TEXT_COLOR = textColor;
        CUSTOM_TEXT_DRAWABLE_FACTORY = customTextDrawableFactory;
    }

    ResourceContainerImageInfo(
            String imagePath,
            String resultName,
            int contentContainerX,
            int contentContainerY,
            int contentContainerWidth,
            int contentContainerHeight,
            int contentContainerPadding,
            Position contentPosition,
            @Nullable String contentClipShapeFilePath,
            boolean isBackground,
            @Nullable Color fill,
            String fontName,
            Color textColor,
            int maxFontSize,
            @Nullable Function<String, Drawable> customTextDrawableFactory
    ) {
        this(
                imagePath,
                resultName,
                contentContainerX,
                contentContainerY,
                contentContainerWidth,
                contentContainerHeight,
                contentContainerPadding,
                contentPosition,
                contentContainerX,
                contentContainerY,
                contentContainerWidth,
                contentContainerHeight,
                contentContainerPadding,
                contentPosition,
                contentClipShapeFilePath,
                isBackground,
                fill,
                fontName,
                textColor,
                maxFontSize,
                customTextDrawableFactory
        );
    }

    @Override
    public ImageMedia getImage() throws IOException {
        return ImageUtil.getImageResourceInRootPackage(IMAGE_PATH);
    }

    @Override
    public String getResultName() {
        return RESULT_NAME;
    }

    @Override
    public int getImageContentX() {
        return IMAGE_CONTENT_X;
    }

    @Override
    public int getImageContentY() {
        return IMAGE_CONTENT_Y;
    }

    @Override
    public int getImageContentWidth() {
        return IMAGE_CONTENT_WIDTH;
    }

    @Override
    public int getImageContentHeight() {
        return IMAGE_CONTENT_HEIGHT;
    }

    @Override
    public Position getImageContentPosition() {
        return IMAGE_CONTENT_POSITION;
    }

    @Override
    public int getTextContentX() {
        return TEXT_CONTENT_X;
    }

    @Override
    public int getTextContentY() {
        return TEXT_CONTENT_Y;
    }

    @Override
    public int getTextContentWidth() {
        return TEXT_CONTENT_WIDTH;
    }

    @Override
    public int getTextContentHeight() {
        return TEXT_CONTENT_HEIGHT;
    }

    @Override
    public Position getTextContentPosition() {
        return TEXT_CONTENT_POSITION;
    }

    @Override
    public Optional<Shape> getContentClip() throws IOException {
        if (CONTENT_CLIP_SHAPE_FILE_PATH == null) {
            return Optional.empty();
        } else {
            return Optional.of(GraphicsUtil.loadShape(CONTENT_CLIP_SHAPE_FILE_PATH));
        }
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

    @Override
    public Optional<Function<String, Drawable>> getCustomTextDrawableFactory() {
        return Optional.ofNullable(CUSTOM_TEXT_DRAWABLE_FACTORY);
    }

    public static void validate() {
        for (ResourceContainerImageInfo containerImageInfo : ResourceContainerImageInfo.values()) {
            validateImage(containerImageInfo);
            validateFont(containerImageInfo);
            validateContentClip(containerImageInfo);
        }
    }

    private static void validateImage(ResourceContainerImageInfo containerImageInfo) {
        if (containerImageInfo.IMAGE_PATH == null) {
            Main.getLogger().error("Image resource path in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is null!");
        } else {
            try {
                FileUtil.validateResourcePathInRootPackage(containerImageInfo.IMAGE_PATH);

                try {
                    ImageUtil.getImageResourceInRootPackage(containerImageInfo.IMAGE_PATH);
                    return;
                } catch (Throwable t) {
                    Main.getLogger().error("Error loading image with path \"" + containerImageInfo.IMAGE_PATH + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\"!", t);
                }
            } catch (Throwable t) {
                Main.getLogger().error("Image resource path \"" + containerImageInfo.IMAGE_PATH + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is invalid!", t);
            }
        }

        Main.shutdown(1);
    }

    private static void validateFont(ResourceContainerImageInfo containerImageInfo) {
        if (containerImageInfo.FONT_NAME == null) {
            Main.getLogger().error("Font name in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is null!");
        } else {
            Font font = new Font(containerImageInfo.FONT_NAME, Font.PLAIN, 1);
            if (font.getFontName().equals(containerImageInfo.FONT_NAME)) {
                return;
            } else {
                Main.getLogger().error("Font \"" + containerImageInfo.FONT_NAME + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is not a registered font!");
            }
        }

        Main.shutdown(1);
    }

    private static void validateContentClip(ResourceContainerImageInfo containerImageInfo) {
        if (containerImageInfo.CONTENT_CLIP_SHAPE_FILE_PATH != null) {
            try {
                FileUtil.validateResourcePathInRootPackage(containerImageInfo.CONTENT_CLIP_SHAPE_FILE_PATH);

                try {
                    GraphicsUtil.loadShape(containerImageInfo.CONTENT_CLIP_SHAPE_FILE_PATH);
                    return;
                } catch (Throwable t) {
                    Main.getLogger().error("Error loading shape with path \"" + containerImageInfo.CONTENT_CLIP_SHAPE_FILE_PATH + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\"!", t);
                }
            } catch (Throwable t) {
                Main.getLogger().error("Shape resource path \"" + containerImageInfo.CONTENT_CLIP_SHAPE_FILE_PATH + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is invalid!", t);
            }

            Main.shutdown(1);
        }
    }
}
