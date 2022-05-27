package io.github.shaksternano.mediamanipulator.exception;

import java.io.IOException;

public class UnsupportedFileFormatException extends IOException {

    public UnsupportedFileFormatException(String message) {
        super(message);
    }
}
