/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

/**
 * Exception cause by the modification
 */
public class ModifyException extends Exception {

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public ModifyException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the root cause of this exception
     */
    public ModifyException(String message, Exception cause) {
        super(message, cause);
    }
}
