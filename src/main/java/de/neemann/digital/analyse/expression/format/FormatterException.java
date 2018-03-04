/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;

/**
 * Error thrown if there is an formatting error
 */
public class FormatterException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the error message
     */
    public FormatterException(String message) {
        super(message);
    }
}
