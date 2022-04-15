package io.github.shaksternano.mediamanipulator.mediamanipulation;

import java.io.File;
import java.io.IOException;

public interface MediaManipulator {

    File caption(File mediaFile, String caption) throws IOException;
}
