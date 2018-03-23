/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

/**
 * Visitor to visit the nodes.
 */
public interface HDLVisitor {
    /**
     * Visits a node
     *
     * @param hdlNode the node to visit
     */
    void visit(HDLNode hdlNode);
}
