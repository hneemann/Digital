package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Part;
import de.neemann.digital.core.PartFactory;

/**
 * @author hneemann
 */
public class Sub extends Add {

    public Sub(int bits) {
        super(bits);
    }

    public static PartFactory createFactory(int bits) {
        return new PartFactory("a", "b", "c_in") {
            @Override
            public Part create() {
                return new Sub(bits);
            }
        };
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValueBits() - b.getValueBits() - c_in.getValueBits();
    }
}
