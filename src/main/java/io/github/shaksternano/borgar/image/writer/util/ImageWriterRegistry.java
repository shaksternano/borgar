package io.github.shaksternano.borgar.image.writer.util;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import io.github.shaksternano.borgar.image.writer.ImageWriter;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ImageWriterRegistry {

    private static final ListMultimap<String, ImageWriter> registry = MultimapBuilder.hashKeys().arrayListValues().build();

    public static void register(ImageWriter writer) {
        for (String format : writer.getSupportedFormats()) {
            registry.put(format, writer);
        }
    }

    public static List<ImageWriter> getWriters(String format) {
        return Collections.unmodifiableList(registry.get(format.toLowerCase()));
    }

    public static Set<String> getSupportedFormats() {
        return Collections.unmodifiableSet(registry.keySet());
    }
}
