package de.neemann.digital.core.switching;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * N-Channel MOS FET
 * Created by hneemann on 22.02.17.
 */
public class NFET extends Relay {
    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(NFET.class, input("G"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL);

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public NFET(ElementAttributes attr) {
        super(attr, false,false);
        getOutput1().setPinDescription(DESCRIPTION);
        getOutput2().setPinDescription(DESCRIPTION);
    }

}
