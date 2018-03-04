/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

/**
 * Exception during creating of KV maps
 */
public class KarnaughException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public KarnaughException(String message) {
        super(message);
    }
}
