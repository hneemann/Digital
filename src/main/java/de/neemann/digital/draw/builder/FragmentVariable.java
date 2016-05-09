package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hneemann
 */
public class FragmentVariable implements Fragment {
    private final Variable variable;
    private final boolean neg;
    private Vector pos;
    private Vector circuitPos;

    public FragmentVariable(Variable variable, boolean neg) {
        this.variable = variable;
        this.neg = neg;
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

    public Vector getCircuitPos() {
        return circuitPos;
    }

    public boolean isNeg() {
        return neg;
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

    public Variable getVariable() {
        return variable;
    }
}
