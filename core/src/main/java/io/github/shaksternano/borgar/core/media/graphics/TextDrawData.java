package io.github.shaksternano.borgar.core.media.graphics;

import io.github.shaksternano.borgar.core.media.graphics.drawable.Drawable;

import java.awt.*;

public record TextDrawData(
    Drawable text,
    int textX,
    int textY,
    int textCentreX,
    int textCentreY,
    Font font
) {
}
