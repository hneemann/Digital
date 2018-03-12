/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Exception thrown during evaluation of the template
 */
public class EvalException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public EvalException(String message) {
        super(message);
    }
}
