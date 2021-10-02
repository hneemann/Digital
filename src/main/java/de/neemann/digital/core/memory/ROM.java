/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.core.memory.rom.ROMInterface;
import de.neemann.digital.core.ValueFormatter;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.io.IOException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A ROM module.
 */
public class ROM extends Node implements Element, ROMInterface, ProgramMemory {

    /**
     * The ROMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ROM.class,
            input("A"),
            input("sel"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.AUTO_RELOAD_ROM)
            .addAttribute(Keys.LAST_DATA_FILE)
            .addAttribute(Keys.BIG_ENDIAN)
            .supportsHDL();

    private DataField data;
    private final ValueFormatter formatter;
    private final ObservableValue output;
    private final int addrBits;
    private final int dataBits;
    private final boolean autoLoad;
    private final boolean isProgramMemory;
    private final ElementAttributes attr;
    private final String label;
    private ObservableValue addrIn;
    private ObservableValue selIn;
    private int addr;
    private boolean sel;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public ROM(ElementAttributes attr) {
        dataBits = attr.get(Keys.BITS);
        output = createOutput1();
        data = attr.get(Keys.DATA);
        addrBits = attr.get(Keys.ADDR_BITS);
        autoLoad = attr.get(Keys.AUTO_RELOAD_ROM);
        if (autoLoad)
            this.attr = attr;
        else
            this.attr = null;
        label = attr.getLabel();
        isProgramMemory = attr.isProgramMemory();
        formatter = attr.getValueFormatter();
    }

    ObservableValue createOutput1() {
        return new ObservableValue("D", dataBits)
                .setToHighZ()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this, 0).addObserverToValue(this);
        selIn = inputs.get(1).checkBits(1, this, 1).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void readInputs() throws NodeException {
        addr = (int) addrIn.getValue();
        sel = selIn.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (sel)
            output.setValue(getDataWord(addr));
        else
            output.setToHighZ();
    }

    long getDataWord(int addr) {
        return data.getDataWord(addr);
    }

    @Override
    public void init(Model model) throws NodeException {
        if (autoLoad) {
            if (attr == null)
                throw new NodeException(Lang.get("err_ROM_noFileGivenToLoad"), this, -1, null);
            try {
                File f = attr.getFile(Keys.LAST_DATA_FILE, model.getRootPath());
                boolean bigEndian = attr.get(Keys.BIG_ENDIAN);
                data = Importer.read(f, dataBits, bigEndian);
            } catch (IOException e) {
                throw new NodeException(e.getMessage(), this, -1, null);
            }
        }
    }


    @Override
    public void setProgramMemory(DataField dataField) {
        setData(dataField);
    }

    /**
     * @return true if this is program memory
     */
    @Override
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    @Override
    public void setData(DataField data) {
        this.data = data;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public ValueFormatter getValueFormatter() {
        return formatter;
    }

    @Override
    public int getDataBits() {
        return dataBits;
    }
}
