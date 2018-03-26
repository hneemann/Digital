/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Used to create separators
 */
public class Separator {
    private final String sep;
    private final CodePrinter out;
    private final String finalizerSeparator;
    private boolean first = true;
    private LineFinalizer lineFinalizer;

    /**
     * Creates a new instance
     *
     * @param out the print stream
     * @param sep The separator
     */
    public Separator(CodePrinter out, String sep) {
        this.sep = sep;
        this.out = out;
        if (sep.length() > 0 && sep.charAt(sep.length() - 1) == '\n')
            finalizerSeparator = sep.substring(0, sep.length() - 1);
        else
            finalizerSeparator = sep;
    }

    /**
     * Inserts the separator
     *
     * @throws IOException IOException
     */
    public void check() throws IOException {
        if (first)
            first = false;
        else {
            if (lineFinalizer == null)
                printSeparator(out);
            else {
                out.print(finalizerSeparator);
                lineFinalizer.finalizeLine(out);
                lineFinalizer = null;
            }
        }
    }

    /**
     * prints the separator
     *
     * @param out the print stream
     * @throws IOException IOException
     */
    public void printSeparator(CodePrinter out) throws IOException {
        out.print(sep);
    }

    /**
     * Sets the line finalizer
     * This finalizer is only used once at the next line ending.
     *
     * @param lineFinalizer thi file finalizer
     */
    public void setLineFinalizer(LineFinalizer lineFinalizer) {
        this.lineFinalizer = lineFinalizer;
    }

    /**
     * Closes this Separator.
     * If there is a pending line separator, it's printed.
     *
     * @throws IOException IOException
     */
    public void close() throws IOException {
        if (lineFinalizer != null)
            lineFinalizer.finalizeLine(out);
    }

    /**
     * If there is a finalizer, this finalizer method is called
     * instead of calling printSeparator.
     * A finalizer is used only once.
     */
    public interface LineFinalizer {
        /**
         * Prints the line ending
         *
         * @param out the stream to print to
         * @throws IOException IOException
         */
        void finalizeLine(CodePrinter out) throws IOException;
    }
}
