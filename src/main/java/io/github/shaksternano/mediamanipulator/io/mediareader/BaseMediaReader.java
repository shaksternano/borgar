package io.github.shaksternano.mediamanipulator.io.mediareader;

import java.io.IOException;
import java.util.NoSuchElementException;

public abstract class BaseMediaReader<T> implements MediaReader<T> {

    protected double frameRate;
    protected int frameCount;
    protected long duration;
    protected double frameDuration;
    protected int audioChannels;
    protected int width;
    protected int height;

    @Override
    public double getFrameRate() {
        return frameRate;
    }

    @Override
    public int getFrameCount() {
        return frameCount;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public double getFrameDuration() {
        return frameDuration;
    }

    @Override
    public int getAudioChannels() {
        return audioChannels;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public T getFrame(long timestamp) throws IOException {
        long circularTimestamp = timestamp % Math.max(getDuration(), 1);
        setTimestamp(circularTimestamp);
        T frame = getNextFrame();
        if (frame == null) {
            throw new NoSuchElementException("No frame at timestamp " + circularTimestamp);
        } else {
            return frame;
        }
    }
}
