package io.github.shaksternano.mediamanipulator.io.mediareader;

public abstract class BaseMediaReader<T> implements MediaReader<T> {

    private final String format;
    protected double frameRate;
    protected int frameCount;
    protected long duration;
    protected double frameDuration;
    protected int audioChannels;
    protected int width;
    protected int height;

    public BaseMediaReader(String format) {
        this.format = format;
    }

    @Override
    public double frameRate() {
        return frameRate;
    }

    @Override
    public int frameCount() {
        return frameCount;
    }

    @Override
    public boolean animated() {
        return frameCount() > 1;
    }

    @Override
    public boolean empty() {
        return frameCount() < 1;
    }

    @Override
    public long duration() {
        return duration;
    }

    @Override
    public double frameDuration() {
        return frameDuration;
    }

    @Override
    public int audioChannels() {
        return audioChannels;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public String format() {
        return format;
    }
}
