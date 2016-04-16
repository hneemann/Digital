package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import java.util.ArrayList;

/**
 * The NAnd
 * @author hneemann
 */
public class NAnd extends Function {

    /**
     * The NAnd description
     */
    public static final ElementTypeDescription DESCRIPTION = new FanInDescription(NAnd.class);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public NAnd(ElementAttributes attributes) {
        super(attributes.get(Keys.BITS));
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
