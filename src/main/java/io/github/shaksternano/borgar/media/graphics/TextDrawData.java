package io.github.shaksternano.borgar.media.graphics;

import io.github.shaksternano.borgar.media.graphics.drawable.Drawable;

import java.awt.*;

public record TextDrawData(
    Drawable text,
    int textX,
    int textY,
    Font font
) {
}
