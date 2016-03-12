package de.neemann.digital.basic;

import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class NOr extends Function {
    public NOr(int bits) {
        super(bits);
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        int f = 0;
        for (ObservableValue i : inputs) {
            f |= i.getValue();
        }
        return ~f;
    }
}
