package io.github.shaksternano.borgar.core.collect;

import java.util.AbstractList;
import java.util.List;
import java.util.function.Function;

public class MappedList<T, R> extends AbstractList<R> {

    private final List<T> list;
    private final Function<T, R> mapper;

    public MappedList(List<T> list, Function<T, R> mapper) {
        this.list = list;
        this.mapper = mapper;
    }

    @Override
    public R get(int index) {
        return mapper.apply(list.get(index));
    }

    @Override
    public int size() {
        return list.size();
    }
}
