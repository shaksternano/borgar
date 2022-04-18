package io.github.shaksternano.mediamanipulator.mediamanipulator;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface MediaManipulator {

    File caption(File media, String caption) throws IOException;

    File makeGif(File media) throws IOException;

    File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException;

    Set<String> getSupportedExtensions();
}
