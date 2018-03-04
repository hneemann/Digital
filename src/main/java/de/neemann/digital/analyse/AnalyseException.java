/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

/**
 * Exception thrown if there problems analysing the circuit
 */
public class AnalyseException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public AnalyseException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param e the cause
     */
    public AnalyseException(Exception e) {
        super(e);
    }
}
