/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Enforces a power supply
 */
public class PowerSupply extends Node implements Element {

    /**
     * Enforces a power supply
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(PowerSupply.class, input("VDD"), input("GND"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private ObservableValue vcc;
    private ObservableValue gnd;

    /**
     * Creates a new instance
     *
     * @param attributes attributes
     */
    public PowerSupply(ElementAttributes attributes) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        vcc = inputs.get(0).checkBits(1, null, 0).addObserverToValue(this);
        gnd = inputs.get(1).checkBits(1, null, 1).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void readInputs() throws NodeException {
        if (vcc.getValue() != 1 || vcc.isHighZ())
            throw new NodeException(Lang.get("err_errorInPowerSupply", "VCC"), this, 0, vcc.asList());
        if (gnd.getValue() != 0 || gnd.isHighZ())
            throw new NodeException(Lang.get("err_errorInPowerSupply", "GND"), this, 1, gnd.asList());
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

}
