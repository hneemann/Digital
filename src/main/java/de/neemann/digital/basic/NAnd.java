package de.neemann.digital.basic;

import de.neemann.digital.NodeException;
import de.neemann.digital.ObservableValue;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class NAnd extends Function {

    public NAnd(int bits) {
        super(bits);
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        int f = -1;
        for (ObservableValue i : inputs) {
            f &= i.getValue();
        }
        return ~f;
    }
}
