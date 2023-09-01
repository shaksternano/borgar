package io.github.shaksternano.borgar.discord.util;

import com.google.gson.*;

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
     * The Optional will be empty if and only if the given JsonElement is null or the path is invalid.
     */
    public static Optional<JsonElement> getNestedElement(JsonElement jsonElement, String... path) {
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

    /**
     * Gets a {@link JsonElement} located in a {@link JsonArray}
     *
     * @param jsonElement The JsonArray.
     * @param index       The index of the JsonElement.
     * @return An {@link Optional} describing the nested JsonElement.
     * The Optional will be empty if and only if the given JsonElement is null, not a JsonArray, or the index is out of bounds.
     */
    public static Optional<JsonElement> getArrayElement(JsonElement jsonElement, int index) {
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() > index) {
                return Optional.of(jsonArray.get(index));
            }
        }

        return Optional.empty();
    }

    public static Optional<String> getString(JsonElement jsonElement) {
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive primitive = jsonElement.getAsJsonPrimitive();
            if (primitive.isString()) {
                return Optional.of(primitive.getAsString());
            }
        }

        return Optional.empty();
    }
}
