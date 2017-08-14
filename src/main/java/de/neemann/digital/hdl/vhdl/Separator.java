package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Used to create separators
 */
public class Separator {
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
     * @throws IOException IOException
     */
    public void check(CodePrinter out) throws IOException {
        if (first)
            first = false;
        else
            out.print(getSeperator());
    }

    /**
     * @return the separator
     */
    public String getSeperator() {
        return sep;
    }
}
