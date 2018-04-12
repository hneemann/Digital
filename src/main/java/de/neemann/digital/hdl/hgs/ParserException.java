/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Expression thrown by the parser
 */
public class ParserException extends Exception {

    /**
     * Creates a new instance
     *
     * @param message the error message
     */
    public ParserException(String message) {
        super(message);
    }

}
