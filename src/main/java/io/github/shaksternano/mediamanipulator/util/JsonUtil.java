package io.github.shaksternano.mediamanipulator.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

/**
 * Contains static methods for working with JSON.
 */
public class JsonUtil {

    public static final JsonElement EMPTY = JsonParser.parseString("");

    /**
     * Gets a {@link JsonElement} nested in {@link JsonObject}s
     *
     * @param jsonElement The JsonElement to start from
     * @param path        The path to the JsonElement.
     * @return An {@link Optional} describing the nested JsonElement.
     * The optional will be empty if and only if the given JsonElement is null or the path is invalid.
     */
    public static Optional<JsonElement> getNestedElement(@Nullable JsonElement jsonElement, String... path) {
        if (jsonElement == null) {
            return Optional.empty();
        } else {
            JsonElement currentElement = jsonElement;

            Iterator<String> pathIterator = Arrays.asList(path).iterator();
            while (pathIterator.hasNext()) {
                String key = pathIterator.next();

                if (currentElement.isJsonObject()) {
                    currentElement = currentElement.getAsJsonObject().get(key);

                    if (currentElement == null) {
                        return Optional.empty();
                    }
                } else {
                    if (pathIterator.hasNext()) {
                        return Optional.empty();
                    }
                }
            }

            return Optional.of(currentElement);
        }
    }

    /**
     * Gets a {@link JsonElement} located in a {@link JsonArray}
     *
     * @param jsonElement The JsonArray.
     * @param index       The index of the JsonElement.
     * @return An {@link Optional} describing the nested JsonElement.
     * The optional will be empty if and only if the given JsonElement is null, not a JsonArray, or the index is invalid.
     */
    public static Optional<JsonElement> getArrayElement(@Nullable JsonElement jsonElement, int index) {
        if (jsonElement != null && jsonElement.isJsonArray()) {
            JsonElement currentElement = jsonElement.getAsJsonArray().get(index);
            return Optional.ofNullable(currentElement);
        } else {
            return Optional.empty();
        }
    }
}
