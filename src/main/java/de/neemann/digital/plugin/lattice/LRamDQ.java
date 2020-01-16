package de.neemann.digital.plugin.lattice;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMInterface;
import de.neemann.digital.plugin.PluginFun;
import de.neemann.digital.plugin.PluginKeys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * LRamDQ component in Lattice
 */
public class LRamDQ extends Node implements Element, RAMInterface {

    /**
     * The LRamDQ description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDQ.class,
            input("Clock", "clock").setClock(),
            input("ClockEn", "clock enable"),
            input("Reset", "reset"),
            input("We", "write enable"),
            input("Address", "address"),
            input("Data", "write data")
    )
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(PluginKeys.IS_NORMAL)
            .addAttribute(PluginKeys.IS_READ_BEFORE_WRITE)
            .addAttribute(PluginKeys.IS_WRITE_THROUGH)
            .addAttribute(PluginKeys.WITH_OUTPUT_REG);

    // 属性
    private final int dataBits;
    private final int addrBits;
    private final String label;
    private final int size;
    private final boolean isProgramMemory;
    private final boolean isNormal;
    private final boolean isReadBWrite;
    private final boolean isWriteThrough;
    private final boolean withOutputReg;
    private DataField memory;

    // 输入值
    private ObservableValue clock;
    private ObservableValue clockEn;
    private ObservableValue reset;
    private ObservableValue we;
    private ObservableValue address;
    private ObservableValue data;

    // 输出值
    private final ObservableValue q;

    private int vAddress;
    private boolean read = false;
    private boolean lastClk = false;     // 上一个 clock 和当前 clock 与，模拟上升沿

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public LRamDQ(ElementAttributes attr) {
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        label = attr.get(Keys.LABEL);
        size = 1 << addrBits;
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        isNormal = attr.get(PluginKeys.IS_NORMAL);
        isReadBWrite = attr.get(PluginKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(PluginKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(PluginKeys.WITH_OUTPUT_REG);

        PluginFun func = new PluginFun();
        memory = func.createDataField(size);
        q = func.createOutput("Q", dataBits, DESCRIPTION, true);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean vClockEn = clockEn.getBool();
        boolean vClock = clock.getBool();
        boolean vWE = we.getBool();
        if (vClockEn) {
            if (!withOutputReg) {
                if (isNormal) {
                    if (!lastClk && vClock) {
                        vAddress = (int) address.getValue();
                        if (vWE) {      // 写
                            long data = this.data.getValue();
                            memory.setData(vAddress, data);
                            read = false;
                        } else
                            read = true;
                    }
                }
            }
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (!withOutputReg) {
            if (isNormal) {
                boolean vClock = clock.getBool();
                if (!lastClk && vClock) {
                    if (read) {
                        vAddress = (int) address.getValue();
                        q.setValue(memory.getDataWord(vAddress));
                    }
                }

                if (reset.getBool())
                    q.setValue(0);

                lastClk = vClock;
            }
        }
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        clock = inputs.get(0).addObserverToValue(this).checkBits(1, this);
        clockEn = inputs.get(1).addObserverToValue(this).checkBits(1, this);
        reset = inputs.get(2).addObserverToValue(this).checkBits(1, this);
        we = inputs.get(3).addObserverToValue(this).checkBits(1, this);
        address = inputs.get(4).addObserverToValue(this).checkBits(addrBits, this);
        data = inputs.get(5).addObserverToValue(this).checkBits(dataBits, this);
    }

    @Override
    public ObservableValues getOutputs() {
        return q.asList();
    }

    @Override
    public DataField getMemory() {
        return memory;
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
        return dataBits;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
