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
    private Vector pos;

    public FragmentVariable(Variable variable) {
        this.variable = variable;
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
}
