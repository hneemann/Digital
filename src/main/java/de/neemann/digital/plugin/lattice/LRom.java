package de.neemann.digital.plugin.lattice;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ProgramMemory;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.core.memory.rom.ROMInterface;
import de.neemann.digital.plugin.PluginFun;
import de.neemann.digital.plugin.PluginKeys;

import java.io.File;
import java.io.IOException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * LRom 组件
 */

public class LRom extends Node implements Element, ROMInterface, ProgramMemory {
    private final static String LAST_DATA_FILE_KEY = "lastDataFile";

    /**
     * The LRom description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRom.class,
            input("Address", "input Address"),
            input("OutClock", "input outClock").setClock(),
            input("OutClockEn", "input outClockEn"),
            input("Reset", "input reset")
    )
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(PluginKeys.IS_NORMAL)
            .addAttribute(PluginKeys.IS_READ_BEFORE_WRITE)
            .addAttribute(PluginKeys.IS_WRITE_THROUGH)
            .addAttribute(PluginKeys.WITH_OUTPUT_REG)
            .addAttribute(Keys.AUTO_RELOAD_ROM);

    private final IntFormat intFormat;
    private final int dataBits;
    private final int addrBits;
    private final String label;
    private DataField data;
    private final File hexFile;
    private final boolean autoLoad;
    private final boolean isProgramMemory;
    private final boolean isNormal;
    private final boolean isReadBWrite;
    private final boolean isWriteThrough;
    private final boolean withOutputReg;
    private ObservableValue q;
    private ObservableValue addr;
    private ObservableValue oClk;
    private ObservableValue oClkEn;
    private ObservableValue reset;
    private int vAddr;
    private boolean vOClkEn;
    private boolean vOClk;
    private boolean vReset;
    private boolean lastClock = true;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public LRom(ElementAttributes attr) {
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        label = attr.getLabel();
        data = attr.get(Keys.DATA);
        autoLoad = attr.get(Keys.AUTO_RELOAD_ROM);
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        isNormal = attr.get(PluginKeys.IS_NORMAL);
        isReadBWrite = attr.get(PluginKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(PluginKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(PluginKeys.WITH_OUTPUT_REG);
        if (autoLoad)
            hexFile = attr.getFile(LAST_DATA_FILE_KEY);
        else
            hexFile = null;
        intFormat = attr.get(Keys.INT_FORMAT);

        q = new PluginFun().createOutput("Q", dataBits, DESCRIPTION, false);
    }

    @Override
    public void readInputs() throws NodeException {
        vAddr = (int) addr.getValue();
        vOClkEn = oClkEn.getBool();
        vOClk = oClk.getBool();
        vReset = reset.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (!withOutputReg) {
            if (isNormal) {
                if (vOClk && !lastClock) {
                    if (vOClkEn) {
                        vAddr = (int) addr.getValue();
                        q.setValue(data.getDataWord(vAddr));
                    }
                }
            }
            if (vReset)
                q.setValue(0);
            lastClock = vOClk;
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addr = inputs.get(0).addObserverToValue(this).checkBits(addrBits, this);
        oClk = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        oClkEn = inputs.get(2).addObserverToValue(this).checkBits(1, this);
        reset = inputs.get(3).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public void init(Model model) throws NodeException {
        if (autoLoad) {
            try {
                data = Importer.read(hexFile, dataBits);
            } catch (IOException e) {
                throw new NodeException(e.getMessage(), this, -1, null);
            }
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return q.asList();
    }

    @Override
    public boolean isProgramMemory() {
        return isProgramMemory;
    }

    @Override
    public void setProgramMemory(DataField dataField) {
        setData(dataField);
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
    public int getDataBits() {
        return dataBits;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }

    @Override
    public IntFormat getIntFormat() {
        return intFormat;
    }
}
