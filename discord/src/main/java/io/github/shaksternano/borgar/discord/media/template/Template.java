package io.github.shaksternano.borgar.discord.media.template;

import io.github.shaksternano.borgar.discord.media.AudioFrame;
import io.github.shaksternano.borgar.discord.media.ImageFrame;
import io.github.shaksternano.borgar.discord.media.graphics.Position;
import io.github.shaksternano.borgar.discord.media.graphics.TextAlignment;
import io.github.shaksternano.borgar.discord.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.discord.media.io.reader.MediaReader;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public interface Template {

    MediaReader<ImageFrame> getImageReader() throws IOException;

    MediaReader<AudioFrame> getAudioReader() throws IOException;

    String getFormat();

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

    double getContentRotation();

    Optional<Shape> getContentClip() throws IOException;

    boolean isBackground();

    Optional<Color> getFill();
}
