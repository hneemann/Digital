package de.neemann.digital.draw.library;

/**
 * Visits all elements of the elements tree
 * Created by hneemann on 25.03.17.
 */
public interface Visitor {

    /**
     * Called on every node
     *
     * @param libraryNode the node
     */
    void visit(LibraryNode libraryNode);
}
