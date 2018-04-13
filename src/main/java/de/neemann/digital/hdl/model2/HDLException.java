/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.ExceptionWithOrigin;

/**
 * Exception thrown during model building
 */
public class HDLException extends ExceptionWithOrigin {
    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the cause
     */
    public HDLException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public HDLException(String message) {
        super(message);
    }
}
