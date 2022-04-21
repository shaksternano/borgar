package io.github.shaksternano.mediamanipulator.mediamanipulator;

/**
 * Contains registered {@link MediaManipulator}s.
 */
public class MediaManipulators {

    /**
     * The {@link MediaManipulator} that deals with static image files.
     */
    public static final MediaManipulator IMAGE = new StaticImageManipulator();

    /**
     * The {@link MediaManipulator} that deals with GIF files.
     */
    public static final MediaManipulator GIF = new GifManipulator();

    /**
     * Registers all the {@link MediaManipulator}s.
     */
    public static void registerMediaManipulators() {
        MediaManipulatorRegistry.register(
                IMAGE,
                GIF
        );
    }
}
