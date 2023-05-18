package io.github.shaksternano.mediamanipulator.io;

import com.google.common.io.Files;

import java.io.File;

public record NamedFile(File file, String name) {

    public NamedFile(File file) {
        this(file, file.getName());
    }

    public NamedFile(File file, String nameWithoutExtension, String extension) {
        this(file, filename(nameWithoutExtension, extension));
    }

    public String nameWithoutExtension() {
        return Files.getNameWithoutExtension(name);
    }

    public String extension() {
        return Files.getFileExtension(name);
    }

    private static String filename(String nameWithoutExtension, String extension) {
        var extensionWithDot = extension.isBlank() ? "" : "." + extension;
        return nameWithoutExtension + extensionWithDot;
    }
}
