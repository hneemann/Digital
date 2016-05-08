package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;

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
    public Vector output() {
        return new Vector(0, 0);
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
}
