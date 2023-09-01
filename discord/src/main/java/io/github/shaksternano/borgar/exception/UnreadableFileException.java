package io.github.shaksternano.borgar.exception;

import java.io.IOException;

public class UnreadableFileException extends IOException {

    public UnreadableFileException(Throwable cause) {
        super(cause);
    }
}
