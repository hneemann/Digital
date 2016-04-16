package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Sub extends Add {

    /**
     * The subtractors description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Sub.class, input("a"), input("b"), input("c_i"))
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Bits);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Sub(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() - b.getValue() - cIn.getValue();
    }
}
