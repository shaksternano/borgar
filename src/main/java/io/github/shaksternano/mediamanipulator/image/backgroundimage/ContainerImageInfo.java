package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public interface ContainerImageInfo {

    ImageMedia getImage() throws IOException;

    String getResultName();

    int getImageContentX();

    int getImageContentY();

    int getImageContentWidth();

    int getImageContentHeight();

    Position getImageContentPosition();

    int getTextContentX();

    int getTextContentY();

    int getTextContentWidth();

    int getTextContentHeight();

    Position getTextContentPosition();

    TextAlignment getTextContentAlignment();

    Font getFont();

    Color getTextColor();

    Optional<Function<String, Drawable>> getCustomTextDrawableFactory();

    Optional<Shape> getContentClip() throws IOException;

    boolean isBackground();

    Optional<Color> getFill();
}
