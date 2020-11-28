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
 * The JK flip-flop
 */
public class FlipflopJK extends FlipflopBit {

    /**
     * The JK-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("JK_FF", FlipflopJK.class, input("J"), input("C").setClock(), input("K"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE)
            .supportsHDL();

    private ObservableValue jVal;
    private ObservableValue kVal;
    private ObservableValue clockVal;
    private boolean lastClock;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopJK(ElementAttributes attributes) {
        super(attributes, DESCRIPTION);
    }

    /**
     * Creates a new instance
     *
     * @param label the label
     * @param q     the output
     * @param qn    the inverted output
     */
    public FlipflopJK(String label, ObservableValue q, ObservableValue qn) {
        super(label, q, qn);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clock = clockVal.getBool();
        if (clock && !lastClock) {
            boolean j = jVal.getBool();
            boolean k = kVal.getBool();

            if (j && k) setOut(!isOut());
            else if (j) setOut(true);
            else if (k) setOut(false);
        }
        lastClock = clock;
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        jVal = inputs.get(0).checkBits(1, this, 0);
        clockVal = inputs.get(1).addObserverToValue(this).checkBits(1, this, 1);
        kVal = inputs.get(2).checkBits(1, this, 2);
    }

    /**
     * @return the j value
     */
    public ObservableValue getjVal() {
        return jVal;
    }

    /**
     * @return the k value
     */
    public ObservableValue getkVal() {
        return kVal;
    }

    /**
     * @return the clock value
     */
    public ObservableValue getClockVal() {
        return clockVal;
    }

}
