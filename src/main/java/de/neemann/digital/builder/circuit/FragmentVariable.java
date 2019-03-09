/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a variable
 */
public class FragmentVariable implements Fragment {
    private final Variable variable;
    private final boolean invert;
    private Vector pos;
    private Vector circuitPos;

    /**
     * Creates a new variable
     *
     * @param variable the variable
     * @param invert   true if variable needs to be inverted
     */
    public FragmentVariable(Variable variable, boolean invert) {
        this.variable = variable;
        this.invert = invert;
    }

    @Override
    public Box doLayout() {
        return new Box(0, 0);
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector pos, Circuit circuit) {
        circuitPos = pos.add(this.pos);
    }

    /**
     * @return the position of the variable in the circuit
     */
    public Vector getCircuitPos() {
        return circuitPos;
    }

    /**
     * @return true if variable is inverted
     */
    public boolean isInvert() {
        return invert;
    }

    @Override
    public List<Vector> getInputs() {
        return new ArrayList<>();
    }

    @Override
    public List<Vector> getOutputs() {
        ArrayList<Vector> o = new ArrayList<>();
        o.add(pos);
        return o;
    }

    /**
     * @return the variable
     */
    public Variable getVariable() {
        return variable;
    }

    @Override
    public <V extends FragmentVisitor> V traverse(V v) {
        v.visit(this);
        return v;
    }
}
