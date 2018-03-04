/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.parser;

/**
 * Exception thrown during parsing of expression
 */
public class ParseException extends Exception {
    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public ParseException(String message) {
        super(message);
    }
}
