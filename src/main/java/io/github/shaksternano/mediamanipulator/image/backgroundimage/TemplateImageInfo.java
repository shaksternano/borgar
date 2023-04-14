package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.graphics.Position;
import io.github.shaksternano.mediamanipulator.graphics.TextAlignment;
import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;
import io.github.shaksternano.mediamanipulator.image.AudioFrame;
import io.github.shaksternano.mediamanipulator.image.ImageFrame;
import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;
import io.github.shaksternano.mediamanipulator.io.mediareader.MediaReader;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public interface TemplateImageInfo {

    ImageMedia getImage() throws IOException;

    MediaReader<ImageFrame> getImageReader() throws IOException;

    MediaReader<AudioFrame> getAudioReader() throws IOException;

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
