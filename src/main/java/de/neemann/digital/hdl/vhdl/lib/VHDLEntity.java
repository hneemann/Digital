/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * Responsible for writing an VHDL entity
 */
public interface VHDLEntity {

    /**
     * Writes the entity to the vhdl file.
     *
     * @param out  the code printer
     * @param node the node
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    void writeEntity(CodePrinter out, HDLNode node) throws IOException, HDLException;

    /**
     * Gets the name of the entity.
     * The name may depend on the node.
     *
     * @param node the node
     * @return the name
     * @throws HDLException HDLException
     */
    String getName(HDLNode node) throws HDLException;

    /**
     * Writes the declaration.
     *
     * @param out  the code printer
     * @param node the node
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException;

    /**
     * Writes the generic map of this node.
     * No semicolon at the end!
     *
     * @param out  the output stream
     * @param node the node
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException;

}
