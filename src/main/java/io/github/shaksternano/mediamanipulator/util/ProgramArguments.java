package io.github.shaksternano.mediamanipulator.util;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ProgramArguments {

    private final Map<String, String> ARGUMENTS;

    public ProgramArguments(String[] args) {
        ARGUMENTS = parseArguments(args);
    }

    private static Map<String, String> parseArguments(String[] args) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        for (String arg : args) {
            String[] parts = arg.split(Pattern.quote("="), 2);

            if (parts.length >= 2) {
                builder.put(parts[0], parts[1]);
            }
        }

        return builder.buildKeepingLast();
    }

    public Optional<String> getArgumentOrEnvironmentVariable(String key) {
        String value = ARGUMENTS.get(key);

        if (value == null) {
            value = System.getenv(key);
        }

        return Optional.ofNullable(value);
    }
}
