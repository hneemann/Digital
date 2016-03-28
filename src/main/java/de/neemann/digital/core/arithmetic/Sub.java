package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Sub extends Add {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Sub.class, "a", "b", "c_i")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Bits);

    public Sub(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() - b.getValue() - c_in.getValue();
    }
}
