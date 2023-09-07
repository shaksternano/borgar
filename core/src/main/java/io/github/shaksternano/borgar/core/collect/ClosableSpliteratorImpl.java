package io.github.shaksternano.borgar.core.collect;

import io.github.shaksternano.borgar.core.util.MiscUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ClosableSpliteratorImpl<T> implements ClosableSpliterator<T> {

    private final Spliterator<T> spliterator;
    private final List<AutoCloseable> toClose = new ArrayList<>();

    public ClosableSpliteratorImpl(Spliterator<T> spliterator) {
        this.spliterator = spliterator;
        if (spliterator instanceof AutoCloseable closeable) {
            toClose.add(closeable);
        }
    }

    public ClosableSpliteratorImpl(Iterator<T> iterator, long size, int characteristics) {
        this(Spliterators.spliterator(iterator, size, characteristics));
        if (iterator instanceof AutoCloseable closeable) {
            toClose.add(closeable);
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return spliterator.tryAdvance(action);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        spliterator.forEachRemaining(action);
    }

    @Override
    public Spliterator<T> trySplit() {
        return spliterator.trySplit();
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
        return spliterator.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }

    @Override
    public boolean hasCharacteristics(int characteristics) {
        return spliterator.hasCharacteristics(characteristics);
    }

    @Override
    public Comparator<? super T> getComparator() {
        return spliterator.getComparator();
    }

    @Override
    public void close() throws IOException {
        MiscUtil.closeAll(toClose);
    }
}
