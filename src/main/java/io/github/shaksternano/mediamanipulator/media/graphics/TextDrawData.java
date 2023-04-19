package io.github.shaksternano.mediamanipulator.media.graphics;

import io.github.shaksternano.mediamanipulator.media.graphics.drawable.Drawable;

import java.awt.*;

public record TextDrawData(
    Drawable text,
    int textX,
    int textY,
    Font font
) {
}
