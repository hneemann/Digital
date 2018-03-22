/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

public class HDLException extends Exception {
    public HDLException(String message, Exception cause) {
        super(message, cause);
    }

    public HDLException(String message) {
        super(message);
    }
}
