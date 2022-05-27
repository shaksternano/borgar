package io.github.shaksternano.mediamanipulator.mediamanipulator.util;

import io.github.shaksternano.mediamanipulator.mediamanipulator.ImageManipulator;
import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;

/**
 * Contains registered {@link MediaManipulator}s.
 */
@SuppressWarnings("unused")
public class MediaManipulators {

    /**
     * Registers all the {@link MediaManipulator}s.
     */
    public static void registerMediaManipulators() {
        MediaManipulatorRegistry.register(new ImageManipulator());
    }
}
