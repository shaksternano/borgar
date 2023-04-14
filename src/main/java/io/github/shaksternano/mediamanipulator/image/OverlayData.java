package io.github.shaksternano.mediamanipulator.image;

public record OverlayData(
    int overlaidWidth,
    int overlaidHeight,
    int image1X,
    int image1Y,
    int image2X,
    int image2Y,
    int overlaidImageType
) {
}
