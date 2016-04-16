package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The XNOr
 * @author hneemann
 */
public class XNOr extends XOr {

    /**
     * The XNOr description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(XNOr.class, input("a"), input("b"))
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.Bits);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public XNOr(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = ~(a.getValue() ^ b.getValue());
    }
}
