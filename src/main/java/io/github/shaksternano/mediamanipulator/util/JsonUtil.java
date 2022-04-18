package io.github.shaksternano.mediamanipulator.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class JsonUtil {

    public static final JsonElement EMPTY_JSON = JsonParser.parseString("");

    public static Optional<JsonElement> getNestedElement(@Nullable JsonElement jsonElement, String... path) {
        if (jsonElement == null) {
            return Optional.empty();
        } else {
            JsonElement currentElement = jsonElement;
            for (String key : path) {
                if (currentElement.isJsonObject()) {
                    currentElement = currentElement.getAsJsonObject().get(key);

                    if (currentElement == null) {
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
