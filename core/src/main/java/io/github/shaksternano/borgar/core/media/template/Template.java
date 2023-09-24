package io.github.shaksternano.borgar.core.media.template;

import io.github.shaksternano.borgar.core.media.AudioFrameOld;
import io.github.shaksternano.borgar.core.media.ImageFrameOld;
import io.github.shaksternano.borgar.core.media.graphics.Position;
import io.github.shaksternano.borgar.core.media.graphics.TextAlignment;
import io.github.shaksternano.borgar.core.media.graphics.drawable.Drawable;
import io.github.shaksternano.borgar.core.media.readerold.MediaReader;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

public interface Template {

    MediaReader<ImageFrameOld> getImageReader() throws IOException;

    MediaReader<AudioFrameOld> getAudioReader() throws IOException;

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
