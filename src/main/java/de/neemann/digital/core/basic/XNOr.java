package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class XNOr extends XOr {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(XNOr.class, "a", "b").addAttribute(AttributeKey.Bits);

    public XNOr(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = ~(a.getValue() ^ b.getValue());
    }
}
