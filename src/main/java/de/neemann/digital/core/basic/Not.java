/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.basic;

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
 * The Not
 */
public class Not extends Node implements Element, Countable {

    /**
     * The Not description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Not.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.WIDE_SHAPE)
            .addAttribute(Keys.BITS)
            .supportsHDL();

    private final ObservableValue output;
    private final int bits;
    private ObservableValue input;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Not(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        output = new ObservableValue("out", bits).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        value = input.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(~value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    /**
     * @return the output
     */
    public ObservableValue getOutput() {
        return output;
    }

    @Override
    public int getDataBits() {
        return bits;
    }

    @Override
    public int getInputsCount() {
        return 1;
    }
}
