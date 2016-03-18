package de.neemann.digital.core.basic;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class XNOr extends XOr {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(XNOr.class, "a", "b").addAttribute(AttributeKey.Bits);

    public XNOr(PartAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        value = ~(a.getValue() ^ b.getValue());
    }
}
