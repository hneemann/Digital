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

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Delay.
 * Allows to delay a signal propagation by a certain amount of time.
 * This time is given in units of gate delays.
 */
public class Delay extends Node implements Element {

    /**
     * The Delay description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Delay.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.DELAY_TIME);

    private final ObservableValue output;
    private final int bits;
    private final int delayTime;
    private ObservableValue input;
    private long[] value;
    private int pos;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Delay(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        output = new ObservableValue("out", bits).setPinDescription(DESCRIPTION);
        int dt = attributes.get(Keys.DELAY_TIME);
        if (dt < 1)
            delayTime = 1;
        else
            delayTime = dt;

        value = new long[delayTime];
    }

    @Override
    public void readInputs() throws NodeException {
        value[pos] = input.getValue();
        pos++;
        if (pos >= delayTime)
            pos = 0;
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value[pos]);

        if (delayTime > 1) {
            boolean same = true;
            for (int i = 1; i < delayTime; i++)
                if (value[0] != value[i]) {
                    same = false;
                    break;
                }
            if (!same)
                hasChanged();
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(bits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

}
