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
     * Writes the header
     *
     * @param out  the code printer
     * @param node the node
     * @throws IOException IOException
     */
    void writeHeader(CodePrinter out, HDLNode node) throws IOException;

    /**
     * Gets the nema of the entity.
     * The name may depend on the node.
     *
     * @param node the node
     * @return the name
     * @throws HDLException HDLException
     */
    String getName(HDLNode node) throws HDLException;

    /**
     * Returns true if this node needs to create a entity
     *
     * @param node the node
     * @return true if entity needs to be written
     */
    boolean needsOutput(HDLNode node);

    /**
     * Writes the declaration
     *
     * @param out  the code printer
     * @param node the node
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException;

    /**
     * Writes the architecture
     *
     * @param out  the code printer
     * @param node the node
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException;

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

    /**
     * Returns true if this entity creates signals in the architecture
     *
     * @param node the node
     * @return true if this entity creates signals in the architecture
     */
    default boolean createsSignals(HDLNode node) {
        return false;
    }

    /**
     * The descriptiob of this entity
     *
     * @param node the node
     * @return the description
     */
    String getDescription(HDLNode node);
}
