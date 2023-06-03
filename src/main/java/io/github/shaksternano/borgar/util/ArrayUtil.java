package io.github.shaksternano.borgar.util;

import java.lang.reflect.Array;

public class ArrayUtil {

    @SuppressWarnings("unchecked")
    public static <T> T[] createNewOrReuse(T[] array, int length) {
        return array.length == length
            ? array
            : (T[]) Array.newInstance(array.getClass().getComponentType(), length);
    }
}
