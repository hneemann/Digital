/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A look up table which can be used as a generic customizable gate.
 */
public class LookUpTable extends Node implements Element {

    /**
     * The LUTs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LookUpTable.class) {
        @Override
        public PinDescriptions getInputDescription(ElementAttributes elementAttributes) {
            int size = elementAttributes.get(Keys.INPUT_COUNT);
            PinDescription[] names = new PinDescription[size];
            for (int i = 0; i < size; i++)
                names[i] = input(Integer.toString(i), Lang.get("elem_LookUpTable_pin_in", i));
            return new PinDescriptions(names);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LUT_INPUT_COUNT)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .supportsHDL();

    private final DataField data;
    private final ObservableValue output;
    private ObservableValues inputs;
    private int addr;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public LookUpTable(ElementAttributes attr) {
        int bits = attr.get(Keys.BITS);
        output = new ObservableValue("out", bits).setPinDescription(DESCRIPTION);
        data = attr.get(Keys.DATA);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.inputs = inputs;
        for (ObservableValue v : inputs)
            v.checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        addr = 0;
        int mask = 1;
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).getValue() > 0)
                addr = addr | mask;
            mask = mask * 2;
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(data.getDataWord(addr));
    }

}
