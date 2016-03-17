package de.neemann.digital.core.basic;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class NAnd extends Function {

    public NAnd(int bits) {
        super(bits);
    }

    public static PartDescription createFactory(int bits, int inputs) {
        return new FanInDescription(new GenericShape("&", inputs).invert(true), inputs, new PartFactory() {
            @Override
            public Part create() {
                return new NAnd(bits);
            }
        });
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
