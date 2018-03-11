/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

/**
 * Listener to notify library uses if the library changes
 */
public interface LibraryListener {

    /**
     * called if library changes
     *
     * @param node the node that has changed. If null the tree structure has changed
     */
    void libraryChanged(LibraryNode node);
}
