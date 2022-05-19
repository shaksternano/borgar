package io.github.shaksternano.mediamanipulator.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LimitedStringBuilder {

    private final int MAX_LENGTH;
    private final List<String> parts = new ArrayList<>();
    private final List<String> partsView = Collections.unmodifiableList(parts);

    public LimitedStringBuilder(int maxLength) {
        MAX_LENGTH = maxLength;
    }

    public LimitedStringBuilder append(Object part) {
        return append(String.valueOf(part));
    }

    public LimitedStringBuilder append(String part) {
        if (parts.isEmpty()) {
            parts.add(part);
        } else {
            String lastPart = parts.get(parts.size() - 1);
            if (lastPart.length() + part.length() > MAX_LENGTH) {
                if (part.length() > MAX_LENGTH) {
                    String nextPart = part;
                    while (nextPart.length() > MAX_LENGTH) {
                        parts.add(nextPart.substring(0, MAX_LENGTH));
                        nextPart = nextPart.substring(MAX_LENGTH);
                    }
                    parts.add(nextPart);
                } else {
                    parts.add(part);
                }
            } else {
                parts.set(parts.size() - 1, lastPart + part);
            }
        }

        return this;
    }

    public List<String> getParts() {
        return partsView;
    }

    @Override
    public int hashCode() {
        return Objects.hash(MAX_LENGTH, parts);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof LimitedStringBuilder other) {
            return Objects.equals(MAX_LENGTH, other.MAX_LENGTH) &&
                    Objects.equals(parts, other.parts);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[Max Length: " + MAX_LENGTH + ", Parts: " + parts + "]";
    }
}
