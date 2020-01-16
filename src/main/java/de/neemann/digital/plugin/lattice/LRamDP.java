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
 * LRamDP
 */
public class LRamDP extends Node implements Element, RAMInterface {

    /**
     * The LRamDP description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDP.class,
            input("WrAddress", "input Write Address"),
            input("RdAddress", "input Read Address"),
            input("Data", "input write Data"),
            input("WE", "input WE"),
            input("RdClockEn", "input RdClockEn"),
            input("RdClock", "input RdClk").setClock(),
            input("WrClockEn", "input WrClockEn"),
            input("WrClock", "input WrClock").setClock(),
            input("Reset", "reset").setClock()
    )
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(PluginKeys.IS_NORMAL)
            .addAttribute(PluginKeys.IS_READ_BEFORE_WRITE)
            .addAttribute(PluginKeys.IS_WRITE_THROUGH)
            .addAttribute(PluginKeys.WITH_OUTPUT_REG);

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
    private final ObservableValue q;
    private ObservableValue wrAdd;
    private ObservableValue rdAdd;
    private ObservableValue data;
    private ObservableValue we;
    private ObservableValue rdClkEn;
    private ObservableValue wrClkEn;
    private ObservableValue wrClk;
    private ObservableValue rdClk;
    private ObservableValue reset;

    private boolean lastWClock = false;
    private boolean lastRClock = false;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public LRamDP(ElementAttributes attr) {
        super(true);
        dataBits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        label = attr.getLabel();
        size = 1 << addrBits;
        isProgramMemory = attr.get(Keys.IS_PROGRAM_MEMORY);
        isNormal = attr.get(PluginKeys.IS_NORMAL);
        isReadBWrite = attr.get(PluginKeys.IS_READ_BEFORE_WRITE);
        isWriteThrough = attr.get(PluginKeys.IS_WRITE_THROUGH);
        withOutputReg = attr.get(PluginKeys.WITH_OUTPUT_REG);

        PluginFun func = new PluginFun();
        memory = func.createDataField(size);
        q = func.createOutput("Q", dataBits, DESCRIPTION, false);
    }

    /**
     * 注册输入信号
     * （当输入信号发生变化时需要调用组件，必须为组件的所有输入添加 addObserverToValue() 方法
     *
     * @param inputs the list of <code>ObservableValue</code>s to use
     */
    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        wrAdd = inputs.get(0).addObserverToValue(this).checkBits(addrBits, this);
        rdAdd = inputs.get(1).addObserverToValue(this).checkBits(addrBits, this);
        data = inputs.get(2).addObserverToValue(this).checkBits(dataBits, this);
        we = inputs.get(3).addObserverToValue(this).checkBits(1, this);
        rdClkEn = inputs.get(4).addObserverToValue(this).checkBits(1, this);
        rdClk = inputs.get(5).addObserverToValue(this).checkBits(1, this);
        wrClkEn = inputs.get(6).addObserverToValue(this).checkBits(1, this);
        wrClk = inputs.get(7).addObserverToValue(this).checkBits(1, this);
        reset = inputs.get(8).addObserverToValue(this).checkBits(1, this);
    }

    /**
     * 当组件值改变时被调用
     */
    @Override
    public void readInputs() throws NodeException {
        boolean vWrClkEn = wrClkEn.getBool();
        boolean vWrClk = wrClk.getBool();
        boolean vWE = we.getBool();
        if (!withOutputReg) {
            if (isNormal) {
                if (vWrClk && !lastWClock) {
                    if (vWE && vWrClkEn) {
                        int vWrAddress = (int) wrAdd.getValue();
                        long data = this.data.getValue();
                        memory.setData(vWrAddress, data);
                    }
                }
                lastWClock = vWrClk;
            }
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        boolean vRdClkEn = rdClkEn.getBool();
        boolean vRdClk = rdClk.getBool();
        if (!withOutputReg) {
            if (isNormal) {
                if (vRdClk && !lastRClock) {
                    if (vRdClkEn) {
                        int vRdAdd = (int) rdAdd.getValue();
                        q.setValue(memory.getDataWord(vRdAdd));
                    }
                }
                if (reset.getBool())
                    q.setValue(0);
                lastRClock = vRdClk;
            }
        }
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
