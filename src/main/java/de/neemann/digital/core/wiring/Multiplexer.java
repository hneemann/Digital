/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.basic.FanIn;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Multiplexer
 */
public class Multiplexer extends FanIn {

    private final int selectorBits;
    private ObservableValue selector;
    private long value;

    /**
     * The Multiplexer description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Multiplexer.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) {
            int size = 1 << elementAttributes.get(Keys.SELECTOR_BITS);
            PinDescription[] names = new PinDescription[size + 1];
            names[0] = input("sel", Lang.get("elem_Multiplexer_pin_sel"));
            for (int i = 0; i < size; i++)
                names[i + 1] = input("in_" + i, Lang.get("elem_Multiplexer_input", i));
            return new PinDescriptions(names);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.SELECTOR_BITS)
            .addAttribute(Keys.FLIP_SEL_POSITON)
            .supportsHDL();

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Multiplexer(ElementAttributes attributes) {
        super(attributes.get(Keys.BITS));
        this.selectorBits = attributes.get(Keys.SELECTOR_BITS);
        getOutput().setDescription(Lang.get("elem_Multiplexer_output"));
    }

    @Override
    public void readInputs() throws NodeException {
        int n = (int) selector.getValue();
        value = getInputs().get(n).getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        getOutput().setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        selector = inputs.get(0).addObserverToValue(this).checkBits(selectorBits, this);
        ObservableValues in = new ObservableValues(inputs, 1, inputs.size());
        super.setInputs(in);

        if (in.size() != (1 << selectorBits))
            throw new BitsException(Lang.get("err_selectorInputCountMismatch"), this, -1, selector);
    }

    @Override
    public int getAddrBits() {
        return selectorBits;
    }
}
