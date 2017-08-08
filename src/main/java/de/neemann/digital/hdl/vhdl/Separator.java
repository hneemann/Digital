package de.neemann.digital.hdl.vhdl;

import java.io.PrintStream;

/**
 * Used to create separators
 */
public final class Separator {
    private final String sep;
    private boolean first = true;

    /**
     * Creates a new instance
     *
     * @param sep The separator
     */
    public Separator(String sep) {
        this.sep = sep;
    }

    /**
     * Inserts the separator
     *
     * @param out the print stream
     */
    public void check(PrintStream out) {
        if (first)
            first = false;
        else
            out.print(sep);
    }
}
