/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Expression thrown by the parser
 * <p>
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserException extends Exception {

    /**
     * Creates a new instance
     *
     * @param message the error message
     * @param cause   cause
     */
    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance
     *
     * @param message the error message
     */
    public ParserException(String message) {
        super(message);
    }

}
