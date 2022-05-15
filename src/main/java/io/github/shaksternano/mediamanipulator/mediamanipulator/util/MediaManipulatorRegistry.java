package io.github.shaksternano.mediamanipulator.mediamanipulator.util;

import io.github.shaksternano.mediamanipulator.mediamanipulator.MediaManipulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Where {@link MediaManipulator}s are registered.
 */
public class MediaManipulatorRegistry {

    /**
     * Stores the registered {@link MediaManipulator}s.
     */
    private static final Map<String, MediaManipulator> registry = new HashMap<>();

    /**
     * Registers {@link MediaManipulator}s.
     *
     * @param manipulators The manipulators to register.
     */
    public static void register(Iterable<MediaManipulator> manipulators) {
        for (MediaManipulator manipulator : manipulators) {
            for (String extension : manipulator.getSupportedExtensions()) {
                registry.put(extension.toLowerCase(), manipulator);
            }
        }
    }

    /**
     * Gets a {@link MediaManipulator} for the given file extension.
     *
     * @param extension The file extension to get the manipulator for.
     * @return An {@link Optional} describing the manipulator.
     * The optional will be empty if and only if no manipulator was
     * registered for the given file extension.
     */
    public static Optional<MediaManipulator> getManipulator(String extension) {
        return Optional.ofNullable(registry.get(extension.toLowerCase()));
    }
}
