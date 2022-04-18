package io.github.shaksternano.mediamanipulator.mediamanipulator;

public class MediaManipulators {

    public static final MediaManipulator IMAGE = new ImageManipulator();
    public static final MediaManipulator GIF = new GifManipulator();

    public static void registerMediaManipulators() {
        MediaManipulatorRegistry.register(
                IMAGE,
                GIF
        );
    }
}
