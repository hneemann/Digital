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
 * LRamDPTrue
 */
public class LRamDPTrue extends Node implements Element, RAMInterface {
    /**
     * The LRamDPTrue description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(LRamDPTrue.class,
            input("DataInA", "data input a"),
            input("DataInB", "data input b"),
            input("AddressA", "address a"),
            input("AddressB", "address b"),
            input("ClockA", "clock a").setClock(),
            input("ClockB", "clock b").setClock(),
            input("ClockEnA", "clock a enable"),
            input("ClockEnB", "clock b enable"),
            input("WrA", "write enable port a"),
            input("WrB", "write enable port b"),
            input("ResetA", "reset a"),
            input("ResetB", "reset b")
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

    // 属性值
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

    // 输出值
    private final ObservableValue qa;
    private final ObservableValue qb;


    // 输入值
    private ObservableValue dataInA;
    private ObservableValue dataInB;
    private ObservableValue addressA;
    private ObservableValue addressB;
    private ObservableValue clockA;
    private ObservableValue clockB;
    private ObservableValue clockEnA;
    private ObservableValue clockEnB;
    private ObservableValue wrA;
    private ObservableValue wrB;
    private ObservableValue resetA;
    private ObservableValue resetB;

    private boolean readA = true;
    private boolean readB = true;
    private boolean lastClockA = true;
    private boolean lastClockB = true;
    private int vAddressA;
    private int vAddressB;

    /**
     * Creates a new instance
     *
     * @param attr the attributes
     */
    public LRamDPTrue(ElementAttributes attr) {
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

        qa = func.createOutput("QA", dataBits, DESCRIPTION, true);
        qb = func.createOutput("QB", dataBits, DESCRIPTION, true);

    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        dataInA = inputs.get(0).addObserverToValue(this).checkBits(dataBits, this);
        dataInB = inputs.get(1).addObserverToValue(this).checkBits(dataBits, this);
        addressA = inputs.get(2).addObserverToValue(this).checkBits(addrBits, this);
        addressB = inputs.get(3).addObserverToValue(this).checkBits(addrBits, this);
        clockA = inputs.get(4).addObserverToValue(this).checkBits(1, this);
        clockB = inputs.get(5).addObserverToValue(this).checkBits(1, this);
        clockEnA = inputs.get(6).addObserverToValue(this).checkBits(1, this);
        clockEnB = inputs.get(7).addObserverToValue(this).checkBits(1, this);
        wrA = inputs.get(8).addObserverToValue(this).checkBits(1, this);
        wrB = inputs.get(9).addObserverToValue(this).checkBits(1, this);
        resetA = inputs.get(10).addObserverToValue(this).checkBits(1, this);
        resetB = inputs.get(11).addObserverToValue(this).checkBits(1, this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean vClockA = clockA.getBool();
        boolean vClockB = clockB.getBool();
        boolean vWrA = wrA.getBool();
        boolean vWrB = wrB.getBool();
        boolean vClockEnA = clockEnA.getBool();
        boolean vClockEnB = clockEnB.getBool();

        if (!withOutputReg) {
            if (isNormal) {
                if (vClockEnA) {
                    if (vClockA && !lastClockA) {
                        vAddressA = (int) addressA.getValue();
                        if (vWrA) {
                            long dataA = dataInA.getValue();
                            memory.setData(vAddressA, dataA);
                            readA = false;
                        } else
                            readA = true;
                    }
                } else
                    readA = false;

                if (vClockEnB) {
                    if (vClockB && !lastClockB) {
                        vAddressB = (int) addressB.getValue();
                        if (vWrB) {
                            long dataB = dataInB.getValue();
                            memory.setData(vAddressB, dataB);
                            readB = false;
                        } else
                            readB = true;
                    }
                } else
                    readB = false;
            }
            lastClockA = vClockA;
            lastClockB = vClockB;
        }
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (!withOutputReg) {
            if (isNormal) {
                if (readA) {
                    vAddressA = (int) addressA.getValue();
                    qa.setValue(memory.getDataWord(vAddressA));
                }
                if (readB) {
                    vAddressB = (int) addressB.getValue();
                    qb.setValue(memory.getDataWord(vAddressB));
                }
                if (resetA.getBool())
                    qa.setValue(0);
                if (resetB.getBool())
                    qb.setValue(0);
            }
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(qa, qb);
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
