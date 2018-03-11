/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Indicates an error backtracking a value to all affected values
 */
public class BacktrackException extends Exception {

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public BacktrackException(String message) {
        super(message);
    }
}
