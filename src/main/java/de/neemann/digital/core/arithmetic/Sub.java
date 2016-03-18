package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class Sub extends Add {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(Sub.class, "a", "b", "c_in").addAttribute(AttributeKey.Bits);

    public Sub(PartAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() - b.getValue() - c_in.getValue();
    }
}
