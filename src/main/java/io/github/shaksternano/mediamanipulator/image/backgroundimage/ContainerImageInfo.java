package io.github.shaksternano.mediamanipulator.image.backgroundimage;

import io.github.shaksternano.mediamanipulator.image.imagemedia.ImageMedia;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;

public interface ContainerImageInfo {

    ImageMedia getImage() throws IOException;

    String getResultName();

    int getContentX();

    int getContentY();

    int getContentWidth();

    int getContentHeight();

    boolean isBackground();

    Optional<Color> getFill();

    Font getFont();

    Color getTextColor();
}
