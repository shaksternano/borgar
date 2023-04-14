package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.Main;
import io.github.shaksternano.mediamanipulator.graphics.GraphicsUtil;
import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.AudioFrame;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.ImageUtil;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.io.FileUtil;
import io.github.shaksternano.mediamanipulator.io.MediaReaders;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public enum ResourceTemplateImageInfo implements TemplateImageInfo {

    SONIC_SAYS(
        "image/containerimage/sonic_says.png",
        "sonic_says",
        210,
        15,
        620,
        330,
        30,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Bitstream Vera Sans",
        Color.WHITE,
        200,
        null,
        null,
        true,
        null
    ),

    SOYJAK_POINTING(
        "image/containerimage/soyjak_pointing.png",
        "soyjak_pointing",
        0,
        100,
        1024,
        450,
        0,
        Position.CENTRE,
        250,
        100,
        750,
        450,
        0,
        Position.CENTRE,
        TextAlignment.CENTER, "Futura-CondensedExtraBold",
        Color.BLACK,
        200,
        null,
        null,
        false,
        null
    ),

    THINKING_BUBBLE(
        "image/containerimage/thinking_bubble.png",
        "thinking_bubble",
        12,
        0,
        128,
        81,
        10,
        Position.CENTRE,
        12,
        0,
        128,
        81,
        20,
        Position.CENTRE,
        TextAlignment.CENTER, "Futura-CondensedExtraBold",
        Color.BLACK,
        50,
        null,
        "shape/thinking_bubble_edge_trimmed.javaobject",
        false,
        Color.WHITE
    ),

    MUTA_SOY(
        "image/containerimage/muta_soy.png",
        "muta_soy",
        400,
        256,
        800,
        768,
        0,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Futura-CondensedExtraBold",
        Color.WHITE,
        200,
        null,
        null,
        false,
        null
    ),

    WALMART_WANTED(
        "image/containerimage/walmart_wanted.png",
        "walmart_wanted",
        428,
        94,
        618,
        318,
        0,
        Position.CENTRE,
        428,
        94,
        618,
        318,
        10,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Futura-CondensedExtraBold",
        Color.BLACK,
        200,
        null,
        null,
        true,
        null
    ),

    OH_MY_GOODNESS_GRACIOUS(
        "image/containerimage/oh_my_goodness_gracious.gif",
        "oh_my_goodness_gracious",
        250,
        350,
        536,
        640,
        0,
        Position.CENTRE,
        250,
        350,
        536,
        640,
        10,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Futura-CondensedExtraBold",
        Color.BLACK,
        200,
        null,
        null,
        true,
        null
    ),

    LIVING_IN_1984(
        "image/containerimage/living_in_1984.png",
        "living_in_1984",
        40,
        6,
        350,
        120,
        10,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Futura-CondensedExtraBold",
        Color.BLACK,
        100,
        null,
        null,
        true,
        null
    ),

    WHO_DID_THIS(
        "image/containerimage/who_did_this.png",
        "wdt",
        0,
        104,
        512,
        403,
        10,
        Position.CENTRE,
        TextAlignment.CENTER,
        "Futura-CondensedExtraBold",
        Color.BLACK,
        100,
        null,
        null,
        true,
        Color.WHITE
    ),
    ;

    private final String imagePath;
    private final String resultName;
    private final int imageContentX;
    private final int imageContentY;
    private final int imageContentWidth;
    private final int imageContentHeight;
    private final Position imageContentPosition;
    private final int textContentX;
    private final int textContentY;
    private final int textContentWidth;
    private final int textContentHeight;
    private final Position textContentPosition;
    private final TextAlignment textContentAlignment;
    private final String fontName;
    private final Font font;
    private final Color textColor;
    @Nullable
    private final Function<String, Drawable> customTextDrawableFactory;
    @Nullable
    private final String contentClipShapeFilePath;
    private final boolean isBackground;
    @Nullable
    private final Color fill;

    ResourceTemplateImageInfo(
        String imagePath,
        String resultName,
        int imageContainerStartX,
        int imageContainerStartY,
        int imageContainerEndX,
        int imageContainerEndY,
        int imageContainerPadding,
        Position imageContentPosition,
        int textContainerStartX,
        int textContainerStartY,
        int textContainerEndX,
        int textContainerEndY,
        int textContainerPadding,
        Position textContentPosition,
        TextAlignment textContentAlignment, String fontName,
        Color textColor,
        int maxFontSize,
        @Nullable Function<String, Drawable> customTextDrawableFactory,
        @Nullable String contentClipShapeFilePath,
        boolean isBackground,
        @Nullable Color fill
    ) {
        this.imagePath = imagePath;
        this.resultName = resultName;
        imageContentX = imageContainerStartX + imageContainerPadding;
        imageContentY = imageContainerStartY + imageContainerPadding;
        textContentX = textContainerStartX + textContainerPadding;
        textContentY = textContainerStartY + textContainerPadding;
        var doubleImagePadding = imageContainerPadding * 2;
        var doubleTextPadding = textContainerPadding * 2;
        imageContentWidth = imageContainerEndX - imageContainerStartX - doubleImagePadding;
        imageContentHeight = imageContainerEndY - imageContainerStartY - doubleImagePadding;
        this.imageContentPosition = imageContentPosition;
        textContentWidth = textContainerEndX - textContainerStartX - doubleTextPadding;
        textContentHeight = textContainerEndY - imageContainerStartY - doubleTextPadding;
        this.textContentPosition = textContentPosition;
        this.textContentAlignment = textContentAlignment;
        this.fontName = fontName;
        font = new Font(this.fontName, Font.PLAIN, maxFontSize);
        this.textColor = textColor;
        this.customTextDrawableFactory = customTextDrawableFactory;
        this.contentClipShapeFilePath = contentClipShapeFilePath;
        this.isBackground = isBackground;
        this.fill = fill;
    }

    ResourceTemplateImageInfo(
        String imagePath,
        String resultName,
        int contentContainerStartX,
        int contentContainerStartY,
        int contentContainerEndX,
        int contentContainerEndY,
        int contentContainerPadding,
        Position contentPosition,
        TextAlignment textContentAlignment,
        String fontName,
        Color textColor,
        int maxFontSize,
        @Nullable Function<String, Drawable> customTextDrawableFactory,
        @Nullable String contentClipShapeFilePath,
        boolean isBackground,
        @Nullable Color fill
    ) {
        this(
            imagePath,
            resultName,
            contentContainerStartX,
            contentContainerStartY,
            contentContainerEndX,
            contentContainerEndY,
            contentContainerPadding,
            contentPosition,
            contentContainerStartX,
            contentContainerStartY,
            contentContainerEndX,
            contentContainerEndY,
            contentContainerPadding,
            contentPosition,
            textContentAlignment, fontName,
            textColor,
            maxFontSize,
            customTextDrawableFactory,
            contentClipShapeFilePath,
            isBackground,
            fill
        );
    }

    @Override
    public ImageMedia getImage() throws IOException {
        return ImageUtil.getImageResourceInRootPackage(imagePath);
    }

    @Override
    public MediaReader<ImageFrame> getImageReader() throws IOException {
        try (var formatStream = FileUtil.getResourceInRootPackage(imagePath)) {
            var format = ImageUtil.getImageFormat(formatStream);
            var inputStream = FileUtil.getResourceInRootPackage(imagePath);
            return MediaReaders.createImageReader(inputStream, format);
        }
    }

    @Override
    public MediaReader<AudioFrame> getAudioReader() throws IOException {
        try (var formatStream = FileUtil.getResourceInRootPackage(imagePath)) {
            var format = ImageUtil.getImageFormat(formatStream);
            var inputStream = FileUtil.getResourceInRootPackage(imagePath);
            return MediaReaders.createAudioReader(inputStream, format);
        }
    }

    @Override
    public String getResultName() {
        return resultName;
    }

    @Override
    public int getImageContentX() {
        return imageContentX;
    }

    @Override
    public int getImageContentY() {
        return imageContentY;
    }

    @Override
    public int getImageContentWidth() {
        return imageContentWidth;
    }

    @Override
    public int getImageContentHeight() {
        return imageContentHeight;
    }

    @Override
    public Position getImageContentPosition() {
        return imageContentPosition;
    }

    @Override
    public int getTextContentX() {
        return textContentX;
    }

    @Override
    public int getTextContentY() {
        return textContentY;
    }

    @Override
    public int getTextContentWidth() {
        return textContentWidth;
    }

    @Override
    public int getTextContentHeight() {
        return textContentHeight;
    }

    @Override
    public Position getTextContentPosition() {
        return textContentPosition;
    }

    @Override
    public TextAlignment getTextContentAlignment() {
        return textContentAlignment;
    }

    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public Color getTextColor() {
        return textColor;
    }

    @Override
    public Optional<Function<String, Drawable>> getCustomTextDrawableFactory() {
        return Optional.ofNullable(customTextDrawableFactory);
    }

    @Override
    public Optional<Shape> getContentClip() throws IOException {
        if (contentClipShapeFilePath == null) {
            return Optional.empty();
        } else {
            return Optional.of(GraphicsUtil.loadShape(contentClipShapeFilePath));
        }
    }

    @Override
    public boolean isBackground() {
        return isBackground;
    }

    @Override
    public Optional<Color> getFill() {
        return Optional.ofNullable(fill);
    }

    public static void validate() {
        for (var containerImageInfo : ResourceTemplateImageInfo.values()) {
            validateImage(containerImageInfo);
            validateFont(containerImageInfo);
            validateContentClip(containerImageInfo);
        }
    }

    private static void validateImage(ResourceTemplateImageInfo containerImageInfo) {
        if (containerImageInfo.imagePath == null) {
            Main.getLogger().error("Image resource path in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is null!");
        } else {
            try {
                FileUtil.validateResourcePathInRootPackage(containerImageInfo.imagePath);

                try {
                    ImageUtil.getImageResourceInRootPackage(containerImageInfo.imagePath);
                    return;
                } catch (Throwable t) {
                    Main.getLogger().error("Error loading image with path \"" + containerImageInfo.imagePath + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\"!", t);
                }
            } catch (Throwable t) {
                Main.getLogger().error("Image resource path \"" + containerImageInfo.imagePath + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is invalid!", t);
            }
        }

        Main.shutdown(1);
    }

    private static void validateFont(ResourceTemplateImageInfo containerImageInfo) {
        if (containerImageInfo.fontName == null) {
            Main.getLogger().error("Font name in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is null!");
        } else {
            var font = new Font(containerImageInfo.fontName, Font.PLAIN, 1);
            if (font.getFontName().equals(containerImageInfo.fontName)) {
                return;
            } else {
                Main.getLogger().error("Font \"" + containerImageInfo.fontName + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is not a registered font!");
            }
        }

        Main.shutdown(1);
    }

    private static void validateContentClip(ResourceTemplateImageInfo containerImageInfo) {
        if (containerImageInfo.contentClipShapeFilePath != null) {
            try {
                FileUtil.validateResourcePathInRootPackage(containerImageInfo.contentClipShapeFilePath);

                try {
                    GraphicsUtil.loadShape(containerImageInfo.contentClipShapeFilePath);
                    return;
                } catch (Throwable t) {
                    Main.getLogger().error("Error loading shape with path \"" + containerImageInfo.contentClipShapeFilePath + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\"!", t);
                }
            } catch (Throwable t) {
                Main.getLogger().error("Shape resource path \"" + containerImageInfo.contentClipShapeFilePath + "\" in " + containerImageInfo.getClass().getSimpleName() + " \"" + containerImageInfo + "\" is invalid!", t);
            }

            Main.shutdown(1);
        }
    }
}
