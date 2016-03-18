package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Or extends Function {

    public static final PartTypeDescription DESCRIPTION = new FanInDescription(Or.class);

    public Or(PartAttributes attributes) {
        super(attributes.get(AttributeKey.Bits));
    }

    @Override
    protected int calculate(ArrayList<ObservableValue> inputs) throws NodeException {
        int f = 0;
        for (ObservableValue i : inputs) {
            f |= i.getValue();
        }
        return f;
    }
}
