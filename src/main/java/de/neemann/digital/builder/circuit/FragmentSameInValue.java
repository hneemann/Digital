/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

import java.util.Arrays;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Takes a fragment and sets all inputs to a single value.
 * Used to create a circuit with a JK ff with J==K.
 */
public class FragmentSameInValue implements Fragment {
    private final Fragment fragment;
    private Vector pos;

    /**
     * Creates a new instance
     *
     * @param fragment the fragment with all inputs are the same value
     */
    public FragmentSameInValue(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public Box doLayout() {
        fragment.setPos(new Vector(SIZE * 2, 0));
        Box b = fragment.doLayout();
        return new Box(2 * SIZE + b.getWidth(), b.getHeight());
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector pos, Circuit circuit) {
        List<Vector> in = Vector.add(fragment.getInputs(), pos);

        Vector last = null;
        for (Vector v : in) {
            Vector p = v.add(-SIZE, 0);
            circuit.add(new Wire(v, p));
            if (last != null)
                circuit.add(new Wire(last, p));
            last = p;
        }

        fragment.addToCircuit(pos, circuit);
    }

    @Override
    public List<Vector> getInputs() {
        int y = fragment.getInputs().get(0).y;
        return Arrays.asList(new Vector(SIZE, y));
    }

    @Override
    public List<Vector> getOutputs() {
        return Vector.add(fragment.getOutputs(), pos);
    }

    @Override
    public <V extends FragmentVisitor> V traverse(V v) {
        v.visit(this);
        fragment.traverse(v);
        return v;
    }
}
