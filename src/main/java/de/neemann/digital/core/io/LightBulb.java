/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A light bulb
 */
public class LightBulb implements Element {

    /**
     * The LED description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(LightBulb.class, input("A"), input("B"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.COLOR);

    /**
     * Creates a new light bulb
     * @param attr the attributes
     */
    public LightBulb(ElementAttributes attr) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        inputs.get(0).checkBits(1, null, 0);
        inputs.get(1).checkBits(1, null, 1);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
    }
}
