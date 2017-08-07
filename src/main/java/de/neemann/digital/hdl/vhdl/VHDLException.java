package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.ExceptionWithOrigin;

/**
 * A VHDL exception
 */
public class VHDLException extends ExceptionWithOrigin {
    /**
     * Creates a new instance
     *
     * @param message message
     */
    public VHDLException(String message) {
        super(message);
    }
}
