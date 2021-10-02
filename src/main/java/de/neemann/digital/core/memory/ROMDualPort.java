/*
 * Copyright (c) 2020 Helmut Neemann.
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
 * The dual ported ROM
 */
public class ROMDualPort extends ROM {

    /**
     * The ROMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ROMDualPort.class,
            input("A1"),
            input("s1"),
            input("A2"),
            input("s2"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.AUTO_RELOAD_ROM)
            .addAttribute(Keys.LAST_DATA_FILE)
            .addAttribute(Keys.BIG_ENDIAN);

    private final ObservableValue output2;
    private ObservableValue addrIn2;
    private ObservableValue selIn2;
    private int addr2;
    private boolean sel2;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ROMDualPort(ElementAttributes attr) {
        super(attr);
        output2 = new ObservableValue("D2", getDataBits())
                .setToHighZ()
                .setPinDescription(DESCRIPTION);

    }

    @Override
    ObservableValue createOutput1() {
        return new ObservableValue("D1", getDataBits())
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        super.setInputs(inputs);
        addrIn2 = inputs.get(2).checkBits(getAddrBits(), this, 2).addObserverToValue(this);
        selIn2 = inputs.get(3).checkBits(1, this, 3).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        super.readInputs();
        addr2 = (int) addrIn2.getValue();
        sel2 = selIn2.getBool();

    }

    @Override
    public void writeOutputs() throws NodeException {
        super.writeOutputs();
        if (sel2)
            output2.setValue(getDataWord(addr2));
        else
            output2.setToHighZ();
    }


    @Override
    public ObservableValues getOutputs() {
        ObservableValue output1 = super.getOutputs().get(0);
        return new ObservableValues(output1, output2);
    }
}
