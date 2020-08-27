/*
 * Copyright (c) 2018 Helmut Neemann
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
 * A register file with two output a one input port.
 */
public class RegisterFile extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RegisterFile.class,
            input("Din"),
            input("we"),
            input("Rw"),
            input("C").setClock(),
            input("Ra"),
            input("Rb"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .supportsHDL();

    private final DataField memory;
    private final ObservableValue out1;
    private final ObservableValue out2;
    private final int addrBits;
    private final int bits;
    private final String label;
    private final int size;
    private ObservableValue reg1In;
    private ObservableValue reg2In;
    private ObservableValue regWIn;
    private ObservableValue data1In;
    private ObservableValue weIn;
    private ObservableValue clk1In;
    private int reg1;
    private int reg2;
    private boolean lastClk = false;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public RegisterFile(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        out1 = new ObservableValue("Da", bits).setPinDescription(DESCRIPTION);
        out2 = new ObservableValue("Db", bits).setPinDescription(DESCRIPTION);
        addrBits = attr.get(Keys.ADDR_BITS);
        size = 1 << addrBits;
        memory = new DataField(size);
        label = attr.getLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.data1In = inputs.get(0).checkBits(bits, this);
        this.weIn = inputs.get(1).checkBits(1, this);
        this.regWIn = inputs.get(2).checkBits(addrBits, this);
        this.clk1In = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        this.reg1In = inputs.get(4).checkBits(addrBits, this).addObserverToValue(this);
        this.reg2In = inputs.get(5).checkBits(addrBits, this).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(out1, out2);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clk = clk1In.getBool();
        boolean str = !lastClk && clk && weIn.getBool();
        if (str) {
            long data = data1In.getValue();
            int regW = (int) regWIn.getValue();
            memory.setData(regW, data);
        }

        reg1 = (int) reg1In.getValue();
        reg2 = (int) reg2In.getValue();

        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        out1.setValue(memory.getDataWord(reg1));
        out2.setValue(memory.getDataWord(reg2));
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
        return false;
    }

    @Override
    public void setProgramMemory(DataField dataField) {
        memory.setDataFrom(dataField);
    }
}
