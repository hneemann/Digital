/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

/**
 * Exception thrown if svg could not be parsed.
 */
public class SvgException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the cause
     */
    public SvgException(String message, Throwable cause) {
        super(message, cause);
    }
}
