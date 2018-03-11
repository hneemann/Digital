/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

/**
 * Visits all elements of the elements tree
 */
public interface Visitor {

    /**
     * Called on every node
     *
     * @param libraryNode the node
     */
    void visit(LibraryNode libraryNode);
}
