/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2.entities;

import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.File;
import java.io.IOException;

/**
 * Represents a vhdl entity
 */
public interface VHDLEntity {
    /**
     * Prints the entity to the printer if not allrady written.
     *
     * @param out  the output to print the code to
     * @param node the node to print
     * @param root the projects main folder
     * @return the vhdl name of the node
     * @throws HGSEvalException HGSEvalException
     * @throws IOException      IOException
     */
    String print(CodePrinter out, HDLNode node, File root) throws HGSEvalException, IOException;

    /**
     * Write the generic map of this node
     *
     * @param out  the output to write to
     * @param node the node
     * @param root the projects main folder
     * @throws IOException IOException
     */
    void writeGenericMap(CodePrinter out, HDLNode node, File root) throws IOException;
}
