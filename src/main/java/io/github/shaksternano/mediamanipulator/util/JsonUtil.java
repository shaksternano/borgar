package io.github.shaksternano.mediamanipulator.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

public class JsonUtil {

    public static final JsonElement EMPTY = JsonParser.parseString("");

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

    public static Optional<JsonElement> getArrayElement(@Nullable JsonElement jsonElement, int index) {
        if (jsonElement != null && jsonElement.isJsonArray()) {
            JsonElement currentElement = jsonElement.getAsJsonArray().get(index);
            return Optional.ofNullable(currentElement);
        } else {
            return Optional.empty();
        }
    }
}
