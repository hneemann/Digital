/*
 * Copyright (c) 2017 Helmut Neemann
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
 * RAM module with a single port to read and write data and a select input.
 * This allows to build a bigger RAM with smaller RAMS and an additional address decoder.
 */
public class RAMSinglePortSel extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMSinglePortSel.class,
            input("A"),
            input("CS"),
            input("WE"),
            input("OE"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.INVERTER_CONFIG);

    private final int bits;
    private final int addrBits;
    private final int size;
    private final String label;
    private final ObservableValue dataOut;
    private final boolean isProgramMemory;
    private DataField memory;
    private ObservableValue addrIn;
    private ObservableValue csIn;
    private ObservableValue weIn;
    private ObservableValue oeIn;
    private ObservableValue dataIn;

    private int addr;
    private boolean cs;
    private boolean oe;
    private boolean we;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public RAMSinglePortSel(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        size = 1 << addrBits;
        memory = new DataField(size);
        label = attr.getLabel();
        dataOut = new ObservableValue("D", bits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION)
                .setBidirectional();
        isProgramMemory = attr.isProgramMemory();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this).addObserverToValue(this);
        csIn = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        weIn = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        oeIn = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        dataIn = inputs.get(4).checkBits(bits, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        cs = csIn.getBool();
        if (cs) {
            addr = (int) addrIn.getValue();
            oe = oeIn.getBool();
            we = weIn.getBool();
            if (we) {
                long data = dataIn.getValue();
                memory.setData(addr, data);
            }
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (cs && oe && !we) {
            dataOut.setValue(memory.getDataWord(addr));
        } else {
            dataOut.setToHighZ();
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return dataOut.asList();
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

    /**
     * Sets the rams data
     *
     * @param data the data to set
     */
    public void setData(DataField data) {
        this.memory = data;
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
