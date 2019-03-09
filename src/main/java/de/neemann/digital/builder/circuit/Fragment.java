/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.List;

/**
 * A fragment used to create a circuit from an expression
 */
public interface Fragment {

    /**
     * Layouts this fragment
     *
     * @return the width and height of this fragment
     */
    Box doLayout();

    /**
     * Sets the position of the fragment.
     * Called during layout
     *
     * @param pos the position
     */
    void setPos(Vector pos);

    /**
     * Fragment is asked to add itself to the given circuit
     *
     * @param pos     the absolute position of the fragment
     * @param circuit the circuit
     */
    void addToCircuit(Vector pos, Circuit circuit);

    /**
     * @return the input positions of this fragment
     */
    List<Vector> getInputs();

    /**
     * @return the output positions of this fragment
     */
    List<Vector> getOutputs();

    /**
     * Visits all fragments
     *
     * @param v   the visitor
     * @param <V> the type of the visitor
     * @return the visitor
     */
    <V extends FragmentVisitor> V traverse(V v);
}
