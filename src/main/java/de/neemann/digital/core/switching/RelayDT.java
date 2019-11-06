/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.stats.Countable;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple relay.
 */
public class RelayDT extends Node implements Element, Countable {

    /**
     * The relays description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RelayDT.class, input("in1"), input("in2"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.POLES);

    private ObservableValue input1;
    private ObservableValue input2;
    private final SwitchDT s;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public RelayDT(ElementAttributes attr) {
        super(false);
        s=new SwitchDT(attr);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input1 = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        input2 = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        s.setInputs(new ObservableValues(inputs, 2, inputs.size()));
    }

    @Override
    public void readInputs() {
        if (input1.isHighZ() || input2.isHighZ())
            s.setClosed(false);
        else
            s.setClosed(input1.getBool() ^ input2.getBool());
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return s.getOutputs();
    }

    @Override
    public void init(Model model) {
        s.init(model);
    }

    /**
     * @return the state of the relay
     */
    public boolean isClosed() {
        return s.isClosed();
    }

    @Override
    public int getDataBits() {
        return s.getDataBits();
    }

    @Override
    public int getInputsCount() {
        return s.getInputsCount();
    }
}
