/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.printer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Pretty printer for code witch handles indentation and writes to a buffer
 */
public class CodePrinterStr extends CodePrinter {

    private final boolean includeLineNumbers;
    private int lineNumber;

    /**
     * Creates a new instance
     *
     * @throws IOException IOException
     */
    public CodePrinterStr() throws IOException {
        this(2, false);
    }


    /**
     * Creates a new instance
     *
     * @param includeLineNumbers if true line numbers are inserted
     * @throws IOException IOException
     */
    public CodePrinterStr(boolean includeLineNumbers) throws IOException {
        this(2, includeLineNumbers);
    }

    /**
     * Creates a new instance with the given indentation
     *
     * @param identWidth         the indentation
     * @param includeLineNumbers if true line numbers are inserted
     * @throws IOException IOException
     */
    public CodePrinterStr(int identWidth, boolean includeLineNumbers) throws IOException {
        super(new ByteArrayOutputStream(), identWidth);
        this.includeLineNumbers = includeLineNumbers;
        if (includeLineNumbers)
            eolIsWritten();
    }

    @Override
    public String toString() {
        try {
            close();
        } catch (IOException e) {
            // can not happen
        }
        return out.toString();
    }

    @Override
    protected void eolIsWritten() throws IOException {
        if (includeLineNumbers) {
            lineNumber++;
            String str = Integer.toString(lineNumber);
            for (int i = 0; i < 3 - str.length(); i++)
                out.write(' ');
            out.write(str.getBytes("utf-8"));
            out.write(' ');
        }
    }
}
