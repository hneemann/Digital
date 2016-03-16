package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.PartDescription;

/**
 * @author hneemann
 */
public class Sub extends Add {

    public Sub(int bits) {
        super(bits);
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(() -> new Sub(bits), "a", "b", "c_in");
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() - b.getValueBits() - c_in.getValueBits();
    }
}
