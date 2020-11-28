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
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

import static de.neemann.digital.core.element.PinInfo.input;


/**
 * The decoder
 */
public class Decoder extends Node implements Element, Countable {

    private final int selectorBits;
    private final ObservableValues output;
    private ObservableValue selector;

    private int oldSelectorValue;
    private int selectorValue;

    /**
     * The Decoder description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Decoder.class,
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.SELECTOR_BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON)
            .supportsHDL();

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Decoder(ElementAttributes attributes) {
        this.selectorBits = attributes.get(Keys.SELECTOR_BITS);
        int outputs = 1 << selectorBits;
        ArrayList<ObservableValue> o = new ArrayList<>(outputs);
        for (int i = 0; i < outputs; i++)
            o.add(new ObservableValue("out_" + i, 1).setValue(0).setDescription(Lang.get("elem_Decoder_output", i)));
        output = new ObservableValues(o);
    }

    @Override
    public ObservableValues getOutputs() {
        return output;
    }

    @Override
    public void readInputs() throws NodeException {
        selectorValue = (int) selector.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.get(oldSelectorValue).setValue(0);
        output.get(selectorValue).setValue(1);
        oldSelectorValue = selectorValue;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        selector = inputs.get(0).addObserverToValue(this).checkBits(selectorBits, this);
    }

    @Override
    public int getDataBits() {
        return 1;
    }

    @Override
    public int getAddrBits() {
        return selectorBits;
    }
}
