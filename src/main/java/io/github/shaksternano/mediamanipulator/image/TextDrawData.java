package io.github.shaksternano.mediamanipulator.image;

import io.github.shaksternano.mediamanipulator.graphics.drawable.Drawable;

import java.awt.*;

public record TextDrawData(
    Drawable text,
    int textX,
    int textY,
    Font font
) {
}
