package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public class Sub extends Add {
    public Sub(ObservableValue a, ObservableValue b, ObservableValue c_in) throws BitsException {
        super(a, b, c_in);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() - b.getValueBits() - c_in.getValueBits();
    }
}
