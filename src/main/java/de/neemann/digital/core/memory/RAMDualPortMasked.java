/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A memory which allows to overwrite single bytes.
 */
public class RAMDualPortMasked extends RAMDualPort {

    private static final long[] MASK_TABLE = new long[256];

    static {
        for (int i = 0; i < 256; i++) {
            long m = 0;
            long bits = 0xff;
            for (int b = 0; b < 8; b++) {
                if ((i & (1 << b)) != 0)
                    m = m | bits;
                bits = bits << 8;
            }
            MASK_TABLE[i] = m;
        }
    }

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMDualPortMasked.class,
            input("A"),
            input("Din"),
            input("str"),
            input("C").setClock(),
            input("ld"),
            input("mask"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.LABEL);

    private final int maskBits;
    private ObservableValue maskVal;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public RAMDualPortMasked(ElementAttributes attr) {
        super(attr);
        maskBits = Math.min(8, (getDataBits() - 1) / 8 + 1);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        super.setInputs(inputs);
        maskVal = inputs.get(5).checkBits(maskBits, this).addObserverToValue(this);
    }

    @Override
    void writeDataToMemory(int addr, long data) {
        DataField memory = getMemory();
        long old = memory.getDataWord(addr);

        long mask = MASK_TABLE[(int) maskVal.getValue()];
        data = data & mask;
        old = old & ~mask;

        memory.setData(addr, data | old);
    }
}
