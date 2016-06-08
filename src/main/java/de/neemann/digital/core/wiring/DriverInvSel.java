package de.neemann.digital.core.wiring;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Driver
 *
 * @author hneemann
 */
public class DriverInvSel extends Driver {

    /**
     * The Driver description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(DriverInvSel.class,
            input("in"),
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public DriverInvSel(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    protected boolean isOutHigh(boolean sel) {
        return sel;
    }
}
