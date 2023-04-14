package io.github.shaksternano.mediamanipulator.mediamanipulator;

import io.github.shaksternano.mediamanipulator.exception.UnsupportedFileFormatException;
import net.dv8tion.jda.api.entities.Guild;
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

    File resize(File media, String fileFormat, float resizeMultiplier, boolean raw, boolean rename) throws IOException;

    File speed(File media, String fileFormat, float speedMultiplier) throws IOException;

    File reduceFps(File media, String fileFormat, int fpsReductionRatio, boolean rename) throws IOException;

    File spin(File media, String fileFormat, float speed, @Nullable Color backgroundColor) throws IOException;

    File compress(File media, String fileFormat, @Nullable Guild guild) throws IOException;

    /**
     * Turns a media file into a GIF file, useful for Discord GIF favoriting.
     *
     * @param media The media file to turn into a GIF.
     * @return The media as a GIF file.
     * @throws IOException                    If there is an error turning the media into a GIF.
     * @throws UncheckedIOException           If there is an error adding the caption.
     * @throws UnsupportedFileFormatException If the operation is not supported by this manipulator.
     */
    File makeGif(File media, String fileFormat, boolean justRenameFile) throws IOException;

    File makePngAndTransparent(File media, String fileFormat) throws IOException;

    File makeIco(File media, String fileFormat) throws IOException;

    /**
     * Gets the set of supported media file extensions that this manipulator supports.
     *
     * @return The set of supported media file extensions that this manipulator supports.
     */
    Set<String> getSupportedExtensions();
}
