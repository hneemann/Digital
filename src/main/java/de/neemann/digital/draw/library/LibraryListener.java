package de.neemann.digital.draw.library;

/**
 * Listener to notify library uses if the library changes
 * Created by hneemann on 25.03.17.
 */
public interface LibraryListener {

    /**
     * called if library changes
     *
     * @param node the node that has changed. If null the tree structure has changed
     */
    void libraryChanged(LibraryNode node);
}
