package de.neemann.digital.hdl.model;

import de.neemann.digital.core.ExceptionWithOrigin;

/**
 * A VHDL exception
 */
public class HDLException extends ExceptionWithOrigin {
    /**
     * Creates a new instance
     *
     * @param message message
     */
    public HDLException(String message) {
        super(message);
    }
}
