/*
 * Copyright (c) 2017 Helmut Neemann
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

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Bit Selector
 */
public class BitSelector extends Node implements Element {

    /**
     * The Bit Selector description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(BitSelector.class,
            input("in"),
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.SELECTOR_BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON)
            .supportsHDL();

    private final ObservableValue output;
    private final int selBits;
    private final int dataBits;
    private ObservableValue input;
    private ObservableValue selIn;
    private long value;
    private long sel;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public BitSelector(ElementAttributes attributes) {
        selBits = attributes.get(Keys.SELECTOR_BITS);
        dataBits = 1 << selBits;
        output = new ObservableValue("out", 1).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
        sel = selIn.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setBool((value & (1L << sel)) != 0);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(dataBits, this);
        selIn = inputs.get(1).addObserverToValue(this).checkBits(selBits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

}
