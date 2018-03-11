/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A multiplier
 */
public class Mul extends Node implements Element {

    /**
     * The multiplier description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Mul.class, input("a"), input("b"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    private final ObservableValue mul;
    private final int bits;
    private ObservableValue a;
    private ObservableValue b;
    private long value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Mul(ElementAttributes attributes) {
        bits = attributes.get(Keys.BITS);
        int outBits = this.bits * 2;
        if (outBits > 64)  // used to avoid strange error conditions. The init method throws the exception
            outBits = 64;
        this.mul = new ObservableValue("mul", outBits).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        value = a.getValue() * b.getValue();
    }

    @Override
    public void writeOutputs() throws NodeException {
        mul.setValue(value);
    }

    @Override
    public void init(Model model) throws NodeException {
        if (bits > 32)
            throw new BitsException(Lang.get("err_toManyBits_Found_N0_maxIs_N1", bits, 32), this);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return mul.asList();
    }

}
