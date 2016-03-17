package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Part;
import de.neemann.digital.core.PartDescription;
import de.neemann.digital.core.PartFactory;
import de.neemann.digital.gui.draw.shapes.GenericShape;

/**
 * @author hneemann
 */
public class Sub extends Add {

    public Sub(int bits) {
        super(bits);
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(new GenericShape("-", 2), new PartFactory() {
            @Override
            public Part create() {
                return new Sub(bits);
            }
        }, "a", "b", "c_in");
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() - b.getValue() - c_in.getValue();
    }
}
