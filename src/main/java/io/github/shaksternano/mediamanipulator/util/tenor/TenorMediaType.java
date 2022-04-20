package io.github.shaksternano.mediamanipulator.util.tenor;

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

    private final String KEY;

    TenorMediaType(String key) {
        KEY = key;
    }

    public String getKey() {
        return KEY;
    }
}
