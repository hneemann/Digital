/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

/**
 * Error thrown during evaluation of an expression
 */
public class ExpressionException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public ExpressionException(String message) {
        super(message);
    }
}
