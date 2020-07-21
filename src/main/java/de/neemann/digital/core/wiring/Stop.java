/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Stop element
 */
public class Stop extends Node implements Element {

    /**
     * The Stop element description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Stop.class, input("stop"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERTER_CONFIG);

    private ObservableValue input;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Stop(ElementAttributes attributes) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        if (input.getBool())
            getModel().close();
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

}
