/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

/**
 */
public class PinMapException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public PinMapException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param e the causing exception
     */
    public PinMapException(Exception e) {
        super(e);
    }
}
