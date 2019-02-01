/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

/**
 * Builder Exception
 */
public class BuilderException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public BuilderException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the cause
     */
    public BuilderException(String message, Exception cause) {
        super(message, cause);
    }
}
