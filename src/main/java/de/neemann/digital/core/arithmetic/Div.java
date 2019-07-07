/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.stats.Countable;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A divider
 */
public class Div extends Node implements Element, Countable {

    /**
     * The dividers description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Div.class, input("a"), input("b"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.SIGNED);

    private final ObservableValue quotient;
    private final ObservableValue remainder;
    private final int bits;
    private final boolean signed;
    private ObservableValue a;
    private ObservableValue b;
    private long q;
    private long r;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Div(ElementAttributes attributes) {
        signed = attributes.get(Keys.SIGNED);
        bits = attributes.get(Keys.BITS);
        this.quotient = new ObservableValue("q", bits).setPinDescription(DESCRIPTION);
        this.remainder = new ObservableValue("r", bits).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        if (signed) {
            long av = a.getValueSigned();
            long bv = b.getValueSigned();
            if (bv == 0) bv = 1;

            q = av / bv;
            r = av % bv;

            // make the remainder positive
            if (r < 0) {
                if (bv >= 0) {
                    r += bv;
                    q--;
                } else {
                    r -= bv;
                    q++;
                }
            }

        } else {
            long av = a.getValue();
            long bv = b.getValue();
            if (bv == 0) bv = 1;

            q = Long.divideUnsigned(av, bv);
            r = Long.remainderUnsigned(av, bv);
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        quotient.setValue(q);
        remainder.setValue(r);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(quotient, remainder);
    }

    @Override
    public int getDataBits() {
        return bits;
    }
}
