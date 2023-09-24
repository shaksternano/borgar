package io.github.shaksternano.borgar.core.media.readerold;

import com.google.common.collect.Iterables;
import io.github.shaksternano.borgar.core.collect.ClosableSpliteratorOld;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

public abstract class BaseMediaReader<E> implements MediaReader<E> {

    private final String format;
    protected double frameRate;
    protected int frameCount;
    protected long duration;
    protected double frameDuration;
    protected int audioChannels;
    protected int audioSampleRate;
    protected int audioBitrate;
    protected int width;
    protected int height;
    protected int loopCount;

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
    public boolean isEmpty() {
        return this.frameCount() == 0;
    }

    @Override
    public boolean isAnimated() {
        return this.frameCount() > 1;
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
    public int audioSampleRate() {
        return audioSampleRate;
    }

    @Override
    public int audioBitrate() {
        return audioBitrate;
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
    public int loopCount() {
        return loopCount;
    }

    @Override
    public String format() {
        return format;
    }

    @Override
    public E first() throws IOException {
        return readFrame(0);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        try (var iterator = iterator()) {
            while (iterator.hasNext()) {
                action.accept(iterator.next());
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public ClosableSpliteratorOld<E> spliterator() {
        int characteristics = Spliterator.ORDERED
            | Spliterator.DISTINCT
            | Spliterator.SORTED
            | Spliterator.NONNULL
            | Spliterator.IMMUTABLE;
        return ClosableSpliteratorOld.create(iterator(), frameCount(), characteristics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            hashElements(),
            format()
        );
    }

    private int hashElements() {
        try (var iterator = iterator()) {
            var hash = 1;
            while (iterator.hasNext()) {
                var element = iterator.next();
                hash = 31 * hash + element.hashCode();
            }
            return hash;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof MediaReader<?> other) {
            return Iterables.elementsEqual(this, other)
                && Objects.equals(format(), other.format());
        } else {
            return false;
        }
    }
}
