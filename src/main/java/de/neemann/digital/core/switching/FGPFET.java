/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * P-Channel floating gate MOS FET
 */
public class FGPFET extends NFET {
    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(FGPFET.class, input("G"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BLOWN);

    private final boolean programmed;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public FGPFET(ElementAttributes attr) {
        super(attr, true);
        getOutput1().setPinDescription(DESCRIPTION);
        getOutput2().setPinDescription(DESCRIPTION);
        programmed = attr.get(Keys.BLOWN);
    }

    @Override
    boolean getClosed(ObservableValue input) {
        if (input.isHighZ() || programmed)
            return false;
        else
            return !input.getBool();
    }
}
