package io.github.shaksternano.mediamanipulator.mediamanipulator;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface MediaManipulator {

    File caption(File media, String caption) throws IOException;

    File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException;

    File overlayMedia(File media, File overlay, int x, int y, boolean expand, @Nullable Color excessColor, @Nullable String overlayName) throws IOException;

    File makeGif(File media) throws IOException;

    Set<String> getSupportedExtensions();
}
