package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;

import java.io.PrintStream;

/**
 * A single VHDL entry.
 * Represents a Digital basic component
 */
public interface VHDLEntity {

    /**
     * Gets the nema of the entity.
     * The name may depend on the node.
     *
     * @param node the node
     * @return the name
     */
    String getName(HDLNode node);

    /**
     * Returns true if this node needs to create a entity
     *
     * @param node the node
     * @return true if entity needs to be written
     */
    boolean needsOutput(HDLNode node);

    /**
     * Prints the inner part - the part between BEGIN and END - of the architecture.
     *
     * @param out  the prot stream to use
     * @param node the node
     */
    void printTo(PrintStream out, HDLNode node);

    /**
     * returns true if the node is a generic node
     *
     * @param node the node
     * @return true if there are generics
     */
    boolean hasGenerics(HDLNode node);

    /**
     * Writes generics to the file
     *
     * @param out  the output stream
     * @param node the node
     * @throws HDLException HDLException
     */
    void writeGenerics(PrintStream out, HDLNode node) throws HDLException;

    /**
     * Writes the genetic port definition.
     *
     * @param out  the output stream
     * @param node the node
     * @throws HDLException HDLException
     */
    void writeGenericPorts(PrintStream out, HDLNode node) throws HDLException;

    /**
     * Writes the generic map of this node.
     * No semicolon at the end!
     *
     * @param out  the output stream
     * @param node the node
     */
    void writeGenericMap(PrintStream out, HDLNode node);

}
