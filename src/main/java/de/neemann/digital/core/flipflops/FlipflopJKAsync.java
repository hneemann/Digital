/*
 * Copyright (c) 2017 Helmut Neemann
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

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The JK Flipflop
 */
public class FlipflopJKAsync extends FlipflopJK {

    /**
     * The JK-FF description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("JK_FF_AS", FlipflopJKAsync.class,
            input("Set"),
            input("J"),
            input("C").setClock(),
            input("K"),
            input("Clr"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DEFAULT)
            .addAttribute(Keys.INVERTER_CONFIG)
            .addAttribute(Keys.VALUE_IS_PROBE)
            .supportsHDL();

    private ObservableValue setVal;
    private ObservableValue clrVal;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public FlipflopJKAsync(ElementAttributes attributes) {
        super(attributes);
    }

    @Override
    public void readInputs() throws NodeException {
        super.readInputs();
        if (setVal.getBool()) setOut(true);
        else if (clrVal.getBool()) setOut(false);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        super.setInputs(ovs(inputs.get(1), inputs.get(2), inputs.get(3)));
        setVal = inputs.get(0).addObserverToValue(this).checkBits(1, this, 0);
        clrVal = inputs.get(4).addObserverToValue(this).checkBits(1, this, 4);
    }

}
