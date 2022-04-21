package io.github.shaksternano.mediamanipulator.mediamanipulator;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

/**
 * Manipulates media files such as images.
 */
public interface MediaManipulator {

    /**
     * Adds a caption to a media file.
     *
     * @param media   The media file to add a caption to.
     * @param caption The caption to add.
     * @return The media file with the caption added.
     * @throws IOException                   If there is an error adding the caption.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File caption(File media, String caption) throws IOException;

    /**
     * Stretches a media file.
     *
     * @param media            The media file to stretch.
     * @param widthMultiplier  The stretch width multiplier.
     * @param heightMultiplier The stretch height multiplier.
     * @return The stretched media file.
     * @throws IOException                   If there is an error stretching the media file.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File stretch(File media, float widthMultiplier, float heightMultiplier) throws IOException;

    File pixelate(File media, int pixelationMultiplier) throws IOException;

    File reduceFps(File media, int fpsReductionRatio) throws IOException;

    /**
     * Overlays a media file on top of another media file.
     *
     * @param media       The media file to be overlaid on.
     * @param overlay     The media file to overlay.
     * @param x           The x coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param y           The y coordinate of the top left corner of the overlay in relation to the media file being overlaid on.
     * @param expand      Whether to expand the resulting media to fit the overlay file.
     * @param expandColor The background color used if the resulting media is expanded.
     * @param overlayName The name of the overlay operation. Used in the overlaid media's file name.
     * @return The media file with the overlay applied.
     * @throws IOException                   If there is an error applying the overlay.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File overlayMedia(File media, File overlay, int x, int y, boolean expand, @Nullable Color expandColor, @Nullable String overlayName) throws IOException;

    /**
     * Turns a media file into a GIF file, useful for Discord GIF favoriting.
     *
     * @param media The media file to turn into a GIF.
     * @return The media as a GIF file.
     * @throws IOException                   If there is an error turning the media into a GIF.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File makeGif(File media) throws IOException;

    /**
     * Gets the set of supported media file extensions that this manipulator supports.
     *
     * @return The set of supported media file extensions that this manipulator supports.
     */
    Set<String> getSupportedExtensions();
}
