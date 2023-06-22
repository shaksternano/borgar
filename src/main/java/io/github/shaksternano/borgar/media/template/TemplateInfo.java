package io.github.shaksternano.borgar.media.template;

import io.github.shaksternano.borgar.media.AudioFrame;
import io.github.shaksternano.borgar.media.ImageFrame;
import io.github.shaksternano.borgar.media.graphics.Position;
import io.github.shaksternano.borgar.media.graphics.TextAlignment;
import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.media.io.reader.MediaReader;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public interface TemplateInfo {

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
