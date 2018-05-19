/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Exception thrown during evaluation of the template
 */
public class HGSEvalException extends Exception {
    private int linNum;

    /**
     * Creates a new instance
     *
     * @param message the message
     */
    public HGSEvalException(String message) {
        super(message);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param cause   the cause
     */
    public HGSEvalException(String message, Exception cause) {
        super(message, cause);
    }

    /**
     * Sets the line number to this expression
     *
     * @param linNum line number
     */
    public void setLinNum(int linNum) {
        if (this.linNum == 0)
            this.linNum = linNum;
    }

    @Override
    public String getMessage() {
        if (linNum == 0)
            return super.getMessage();
        else
            return super.getMessage() + "; line " + linNum;
    }
}
