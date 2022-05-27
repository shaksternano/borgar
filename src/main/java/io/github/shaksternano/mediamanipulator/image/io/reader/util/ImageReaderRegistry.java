package io.github.shaksternano.mediamanipulator.image.io.reader.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import io.github.shaksternano.mediamanipulator.image.io.reader.ImageReader;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImageReaderRegistry {

    private static final ListMultimap<String, ImageReader> registry = MultimapBuilder.hashKeys().arrayListValues().build();

    public static void register(ImageReader reader) {
        for (String format : reader.getSupportedFormats()) {
            registry.put(format, reader);
        }
    }

    public static List<ImageReader> getReaders(String format) {
        return Collections.unmodifiableList(registry.get(format.toLowerCase()));
    }

    public static Set<String> getSupportedFormats() {
        return Collections.unmodifiableSet(registry.keySet());
    }
}
