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
import de.neemann.digital.core.stats.Countable;

import static de.neemann.digital.core.ObservableValues.ovs;
import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A comparator
 */
public class Comparator extends Node implements Element, Countable {
    private static final long MSB = Bits.signedFlagMask(64);
    private static final long LSB = ~MSB;

    /**
     * The comparators description
     */
    public static final ElementTypeDescription DESCRIPTION =
            new ElementTypeDescription(Comparator.class, input("a"), input("b"))
                    .addAttribute(Keys.ROTATE)
                    .addAttribute(Keys.LABEL)
                    .addAttribute(Keys.BITS)
                    .addAttribute(Keys.SIGNED)
                    .setShortName("")
                    .supportsHDL();

    private final int bits;
    private final Boolean signed;
    private final ObservableValue aklb;
    private final ObservableValue equals;
    private final ObservableValue agrb;
    private ObservableValue a;
    private ObservableValue b;
    private long valueA;
    private long valueB;

    /**
     * Create a new instance
     *
     * @param attributes the attributes
     */
    public Comparator(ElementAttributes attributes) {
        signed = attributes.get(Keys.SIGNED);
        bits = attributes.get(Keys.BITS);

        this.agrb = new ObservableValue(">", 1).setPinDescription(DESCRIPTION);
        this.equals = new ObservableValue("=", 1).setPinDescription(DESCRIPTION);
        this.aklb = new ObservableValue("<", 1).setPinDescription(DESCRIPTION);
    }

    @Override
    public void readInputs() throws NodeException {
        if (signed) {
            valueA = a.getValueSigned();
            valueB = b.getValueSigned();
        } else {
            valueA = a.getValue();
            valueB = b.getValue();
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (valueA == valueB) {
            equals.setValue(1);
            aklb.setValue(0);
            agrb.setValue(0);
        } else {
            equals.setValue(0);

            boolean kl;
            if (bits < 64 || signed)
                kl = valueA < valueB;
            else {
                int a = (valueA & MSB) == 0 ? 0 : 1;
                int b = (valueB & MSB) == 0 ? 0 : 1;
                if (a == b) {
                    kl = (valueA & LSB) < (valueB & LSB);
                } else {
                    kl = a < b;
                }
            }
            aklb.setBool(kl);
            agrb.setBool(!kl);
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws BitsException {
        a = inputs.get(0).addObserverToValue(this).checkBits(bits, this, 0);
        b = inputs.get(1).addObserverToValue(this).checkBits(bits, this, 1);
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(agrb, equals, aklb);
    }

    @Override
    public int getDataBits() {
        return bits;
    }
}
