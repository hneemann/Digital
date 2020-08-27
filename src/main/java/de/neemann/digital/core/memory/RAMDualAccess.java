/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with different ports to read and write the data
 * and an additional read port. Used to implement graphic card memory.
 */
public class RAMDualAccess extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMDualAccess.class,
            input("str"),
            input("C").setClock(),
            input("ld"),
            input("1A"),
            input("1Din"),
            input("2A"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.LABEL)
            .supportsHDL();

    private final DataField memory;
    private final ObservableValue out1;
    private final ObservableValue out2;
    private final int addrBits;
    private final int bits;
    private final String label;
    private final int size;
    private final boolean isProgramMemory;
    private ObservableValue addr1In;
    private ObservableValue data1In;
    private ObservableValue str1In;
    private ObservableValue clk1In;
    private ObservableValue ld1In;
    private ObservableValue addr2In;
    private int addr1;
    private int addr2;
    private boolean lastClk = false;
    private boolean ld;

    /**
     * Creates a new instance
     *
     * @param attr the elemets attributes
     */
    public RAMDualAccess(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        out1 = new ObservableValue("1D", bits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
        out2 = new ObservableValue("2D", bits)
                .setPinDescription(DESCRIPTION);
        addrBits = attr.get(Keys.ADDR_BITS);
        size = 1 << addrBits;
        memory = new DataField(size);
        label = attr.getLabel();
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.str1In = inputs.get(0).checkBits(1, this);
        this.clk1In = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        this.ld1In = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        this.addr1In = inputs.get(3).checkBits(addrBits, this).addObserverToValue(this);
        this.data1In = inputs.get(4).checkBits(bits, this);
        this.addr2In = inputs.get(5).checkBits(addrBits, this).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(out1, out2);
    }

    @Override
    public void readInputs() throws NodeException {
        long data = 0;
        boolean clk = clk1In.getBool();
        boolean str;
        if (!lastClk && clk) {
            str = str1In.getBool();
            if (str)
                data = data1In.getValue();
        } else
            str = false;
        ld = ld1In.getBool();
        if (ld || str)
            addr1 = (int) addr1In.getValue();

        if (str)
            memory.setData(addr1, data);

        addr2 = (int) addr2In.getValue();

        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (ld) {
            out1.setValue(memory.getDataWord(addr1));
        } else {
            out1.setToHighZ();
        }
        out2.setValue(memory.getDataWord(addr2));
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
    public int getDataBits() {
        return bits;
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
}
