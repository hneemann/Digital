/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

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
 * A synchronized read memory which can be synthesised by using block ram.
 */
public class BlockRAMDualPort extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(BlockRAMDualPort.class,
            input("A"),
            input("Din"),
            input("str"),
            input("C").setClock())
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.LABEL)
            .supportsHDL();

    private DataField memory;
    private final ObservableValue output;
    private final int addrBits;
    private final int bits;
    private final String label;
    private final int size;
    private final boolean isProgramMemory;
    private ObservableValue str;
    private ObservableValue addrIn;
    private ObservableValue dataIn;
    private ObservableValue clkIn;
    private boolean lastClk = false;
    private long outputVal;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public BlockRAMDualPort(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        output = new ObservableValue("D", bits).setPinDescription(DESCRIPTION);
        addrBits = attr.get(Keys.ADDR_BITS);
        size = 1 << addrBits;
        memory = new DataField(size);
        label = attr.getLabel();
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
    }


    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this);
        dataIn = inputs.get(1).checkBits(bits, this);
        str = inputs.get(2).checkBits(1, this);
        clkIn = inputs.get(3).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clk = clkIn.getBool();
        if (!lastClk && clk) {
            int addr = (int) addrIn.getValue();
            outputVal = memory.getDataWord(addr);
            if (str.getBool())
                memory.setData(addr, dataIn.getValue());
        }
        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.setValue(outputVal);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public DataField getMemory() {
        return memory;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    @Override
    public void setProgramMemory(DataField dataField) {
        memory.setDataFrom(dataField);
    }

    @Override
    public int getDataBits() {
        return bits;
    }

}
