package io.github.shaksternano.mediamanipulator.mediamanipulator;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;

/**
 * Manipulates media files such as images.
 */
public interface MediaManipulator {

    /**
     * Adds a caption to a media file.
     *
     * @param media The media file to add a caption to.
     * @param words The words of the caption.
     * @return The media file with the caption added.
     * @throws IOException                   If there is an error adding the caption.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File caption(File media, String[] words, Map<String, BufferedImage> images) throws IOException;

    /**
     * Stretches a media file.
     *
     * @param media            The media file to stretch.
     * @param widthMultiplier  The stretch width multiplier.
     * @param heightMultiplier The stretch height multiplier.
     * @param raw              If false, extra processing is done to smoothen the resulting image.
     *                         If true, no extra processing is done.
     * @return The stretched media file.
     * @throws IOException                   If there is an error stretching the media file.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File stretch(File media, float widthMultiplier, float heightMultiplier, boolean raw) throws IOException;

    File resize(File media, float resizeMultiplier, boolean raw) throws IOException;

    File speed(File media, float speedMultiplier) throws IOException;

    File pixelate(File media, int pixelationMultiplier) throws IOException;

    File reduceFps(File media, int fpsReductionRatio) throws IOException;

    File speechBubble(File media, boolean cutOut) throws IOException;

    File rotate(File media, float degrees, @Nullable Color backgroundColor) throws IOException;

    File spin(File media, float speed, @Nullable Color backgroundColor) throws IOException;

    File compress(File media) throws IOException;

    /**
     * Turns a media file into a GIF file, useful for Discord GIF favoriting.
     *
     * @param media The media file to turn into a GIF.
     * @return The media as a GIF file.
     * @throws IOException                   If there is an error turning the media into a GIF.
     * @throws UncheckedIOException          If there is an error adding the caption.
     * @throws UnsupportedOperationException If the operation is not supported by this manipulator.
     */
    File makeGif(File media, boolean justRenameFile) throws IOException;

    File makePngOrTransparent(File media) throws IOException;

    File makeIco(File media) throws IOException;

    /**
     * Gets the set of supported media file extensions that this manipulator supports.
     *
     * @return The set of supported media file extensions that this manipulator supports.
     */
    Set<String> getSupportedExtensions();
}
