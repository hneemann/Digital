/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Something which can print itself to a CodePrinter
 */
public interface Printable {
    /**
     * Prints itfels to the CodePrinter
     *
     * @param out the CodePrinter instance
     * @throws IOException IOException
     */
    void print(CodePrinter out) throws IOException;
}
