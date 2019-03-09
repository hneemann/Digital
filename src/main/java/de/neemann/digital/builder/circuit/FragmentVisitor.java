/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

/**
 * Visitor used to visit all fragments
 */
public interface FragmentVisitor {
    /**
     * Is called with all the fragments
     *
     * @param fr the fragment to visit
     */
    void visit(Fragment fr);
}
