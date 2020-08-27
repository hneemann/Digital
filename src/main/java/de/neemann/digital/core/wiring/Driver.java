/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.stats.Countable;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Driver
 */
public class Driver extends Node implements Element, Countable {

    /**
     * The Driver description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Driver.class,
            input("in"),
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON)
            .supportsHDL();

    private final ObservableValue output;
    private final int bits;
    private ObservableValue input;
    private ObservableValue selIn;
    private long value;
    private boolean sel;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Driver(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        output = new ObservableValue("out", bits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
        sel = selIn.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (isOutHighZ(sel))
            output.setToHighZ();
        else
            output.setValue(value);
    }

    /**
     * Returns the highZ state depending of the sel state
     *
     * @param sel the selected input
     * @return the highZ state
     */
    protected boolean isOutHighZ(boolean sel) {
        return !sel;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
        selIn = inputs.get(1).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public int getDataBits() {
        return bits;
    }
}
