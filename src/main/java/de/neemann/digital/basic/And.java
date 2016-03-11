package de.neemann.digital.basic;

import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class And extends Function {

    public And(int bits) {
        super(bits);
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) {
        int f = -1;
        for (ObservableValue i : inputs) {
            f &= i.getValue();
        }
        return f;
    }
}
