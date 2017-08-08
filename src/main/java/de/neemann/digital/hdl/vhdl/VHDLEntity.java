package de.neemann.digital.hdl.vhdl;

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

}
