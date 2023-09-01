package io.github.shaksternano.borgar.discord.media.graphics;

import io.github.shaksternano.borgar.discord.media.graphics.drawable.Drawable;

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
