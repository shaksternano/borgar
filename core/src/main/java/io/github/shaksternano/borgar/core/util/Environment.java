package io.github.shaksternano.borgar.core.util;

import io.github.shaksternano.borgar.core.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {

    private static final Map<String, String> customEnvVars = new HashMap<>();

    public static void load(File file) throws IOException {
        for (var line : Files.readAllLines(file.toPath())) {
            if (!line.isBlank()) {
                var envVar = line.split("=", 2);
                if (envVar.length == 2) {
                    setEnvVar(envVar[0].trim(), envVar[1].trim());
                } else {
                    Main.getLogger().error("Invalid environment variable: " + line);
                }
            }
        }
    }

    public static Optional<String> getEnvVar(String key) {
        var customEnvVar = customEnvVars.get(key);
        if (customEnvVar == null) {
            return Optional.ofNullable(System.getenv(key));
        } else {
            return Optional.of(customEnvVar);
        }
    }

    public static void setEnvVar(String key, String value) {
        customEnvVars.put(key, value);
    }
}
