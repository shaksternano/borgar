package io.github.shaksternano.borgar.core.exception;

import io.github.shaksternano.borgar.core.command.Command;

/**
 * Exception thrown when a {@link Command} is executed without a required argument.
 */
public class MissingArgumentException extends RuntimeException {

    public MissingArgumentException(String message) {
        super(message);
    }
}
