/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

/**
 */
public class TestingDataException extends Exception {
    /**
     * creates a new instance
     *
     * @param message the error message
     * @param cause   the cause
     */
    public TestingDataException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * creates a new instance
     *
     * @param message the message
     */
    public TestingDataException(String message) {
        super(message);
    }
}
