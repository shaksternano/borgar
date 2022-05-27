package io.github.shaksternano.mediamanipulator.exception;

import java.io.IOException;

public class UnreadableFileException extends IOException {

    public UnreadableFileException(String message) {
        super(message);
    }
}
