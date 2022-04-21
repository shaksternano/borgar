package io.github.shaksternano.mediamanipulator.util.tenor;

/**
 * Contains Tenor string constants for different media types.
 */
public enum TenorMediaType {

    GIF_EXTRA_SMALL("nanogif"),
    GIF_SMALL("tinygif"),
    GIF_NORMAL("gif"),
    GIF_LARGE("mediumgif"),

    MP4_EXTRA_SMALL("nanomp4"),
    MP4_SMALL("tinymp4"),
    MP4_NORMAL("mp4"),
    MP4_NORMAL_LOOPED("loopedmp4"),

    WEBM_EXTRA_SMALL("nanowebm"),
    WEBM_SMALL("tinywebm"),
    WEBM_NORMAL("webm");

    /**
     * The Tenor JSON key for the media type.
     */
    private final String KEY;

    /**
     * Creates a new TenorMediaType.
     *
     * @param key The Tenor JSON key for the media type.
     */
    TenorMediaType(String key) {
        KEY = key;
    }

    /**
     * Gets the Tenor JSON key for the media type.
     *
     * @return The Tenor JSON key for the media type.
     */
    public String getKey() {
        return KEY;
    }
}
