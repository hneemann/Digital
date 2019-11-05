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
import de.neemann.digital.draw.elements.PinException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * N-Channel MOS FET
 */
public class NFET extends Node implements Element, Countable {
    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(NFET.class, input("G"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.FET_UNIDIRECTIONAL)
            .addAttribute(Keys.LABEL);

    private final PlainSwitch s;
    private ObservableValue input;
    private boolean closed;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public NFET(ElementAttributes attr) {
        this(attr, false);
        s.getOutput1().setPinDescription(DESCRIPTION);
        s.getOutput2().setPinDescription(DESCRIPTION);
    }

    NFET(ElementAttributes attr, boolean pChan) {
        boolean uniDir = attr.get(Keys.FET_UNIDIRECTIONAL);
        if (pChan) {
            s = new PlainSwitch(attr.getBits(), false, "S", "D");
            if (uniDir) s.setUnidirectional(PlainSwitch.Unidirectional.FROM1TO2);
        } else {
            s = new PlainSwitch(attr.getBits(), false, "D", "S");
            if (uniDir) s.setUnidirectional(PlainSwitch.Unidirectional.FROM2TO1);
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).checkBits(1, this, 0).addObserverToValue(this);
        s.setInputs(inputs.get(1), inputs.get(2));

    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return s.getOutputs();
    }

    @Override
    public void readInputs() throws NodeException {
        closed = getClosed(input);
    }

    /**
     * Determines the state of the FET debending on its input
     *
     * @param input the input
     * @return true if FET is conducting
     */
    boolean getClosed(ObservableValue input) {
        if (input.isHighZ())
            return false;
        else
            return input.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        s.setClosed(closed);
    }

    /**
     * @return true if fet is closed
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void init(Model model) throws NodeException {
        s.init(model);
    }

    /**
     * @return output 1
     */
    ObservableValue getOutput1() {
        return s.getOutput1();
    }

    /**
     * @return output 2
     */
    ObservableValue getOutput2() {
        return s.getOutput2();
    }

    @Override
    public int getDataBits() {
        return s.getBits();
    }
}
