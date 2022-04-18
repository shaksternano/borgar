package io.github.shaksternano.mediamanipulator.mediamanipulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MediaManipulatorRegistry {

    private static final Map<String, MediaManipulator> registry = new HashMap<>();

    public static void register(MediaManipulator... manipulators) {
        for (MediaManipulator manipulator : manipulators) {
            for (String extension : manipulator.getSupportedExtensions()) {
                registry.put(extension.toLowerCase(), manipulator);
            }
        }
    }

    public static Optional<MediaManipulator> getManipulator(String extension) {
        return Optional.ofNullable(registry.get(extension.toLowerCase()));
    }
}
