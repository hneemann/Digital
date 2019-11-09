/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The RS flip-flop
 */
public class FlipflopRS extends FlipflopBit {

    /**
     * The RS-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("RS_FF", FlipflopRS.class, input("S"), input("C").setClock(), input("R"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE);


    private ObservableValue sVal;
    private ObservableValue rVal;
    private ObservableValue clockVal;
    private boolean lastClock;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopRS(ElementAttributes attributes) {
        super(attributes, DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean s = sVal.getBool();
            boolean r = rVal.getBool();

            if (s) {
                if (r) {
                    setOut(Math.random()<0.5);
                } else {
                    setOut(true);
                }
            } else {
                if (r) {
                    setOut(false);
                }
            }
        }
        lastClock = clock;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        sVal = inputs.get(0).checkBits(1, this, 0);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        rVal = inputs.get(2).checkBits(1, this, 2);
    }

}
