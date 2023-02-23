package io.github.shaksternano.mediamanipulator.image;

import java.awt.image.BufferedImage;

/**
 * Represents a single frame of an image.
 *
 * @param image     The image of the frame.
 * @param duration  The duration of the frame in microseconds.
 * @param timestamp The timestamp of the frame in microseconds.
 */
public record ImageFrame(BufferedImage image, long duration, long timestamp) {
}
