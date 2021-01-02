/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import java.util.Random;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Creates a random number using the Java Random class.
 */
public class PRNG extends Node implements Element {

    /**
     * The element type description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(PRNG.class,
            input("S"),
            input("se"),
            input("ne"),
            input("C").setClock())
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL);

    private final ObservableValue output;
    private final int bits;
    private final long mask;
    private final Random random;
    private ObservableValue seedVal;
    private ObservableValue setVal;
    private ObservableValue nextVal;
    private ObservableValue clockVal;
    private boolean lastClock;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the elements attributes
     */
    public PRNG(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        output = new ObservableValue("R", bits).setPinDescription(DESCRIPTION);
        // Let Java set the initial seed so that different values are generated each simulation,
        // unless a specific seed is set by the user later on.
        random = new Random();
        mask = Bits.mask(bits);
        value = random.nextLong() & mask;
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();

        if (clock && !lastClock) {
            // First update seed
            if (setVal.getBool())
                random.setSeed(seedVal.getValue());

            // Then value. This keeps the component well defined in case both 'set' and 'next' inputs are set.
            if (nextVal.getBool())
                value = random.nextLong() & mask;
        }

        lastClock = clock;
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(value);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        // Note: could separate bit count for seed input & value output
        seedVal = inputs.get(0).checkBits(bits, this);
        setVal = inputs.get(1).checkBits(1, this);
        nextVal = inputs.get(2).checkBits(1, this);
        clockVal = inputs.get(3).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

}
