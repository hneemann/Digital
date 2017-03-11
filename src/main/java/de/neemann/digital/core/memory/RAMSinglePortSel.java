package de.neemann.digital.core.memory;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with a single port to read and write data and a select input.
 * This allows to build a bigger RAM with smaller RAMS and an additional address decoder.
 *
 * @author hneemann
 */
public class RAMSinglePortSel extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMSinglePortSel.class,
            input("A"),
            input("sel"),
            input("C"),
            input("W/\u00ACR"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL);

    private final int bits;
    private final int addrBits;
    private final int size;
    private final DataField memory;
    private final String label;
    private final ObservableValue dataOut;
    private ObservableValue addrIn;
    private ObservableValue selIn;
    private ObservableValue clkIn;
    private ObservableValue wnrIn;
    private ObservableValue dataIn;

    private boolean lastClk = false;
    private boolean sel;
    private int addr;
    private boolean write;

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
        label = attr.getCleanLabel();
        dataOut = new ObservableValue("D", bits, true).setPinDescription(DESCRIPTION).setBidirectional(true);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this).addObserverToValue(this);
        selIn = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        clkIn = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        wnrIn = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        dataIn = inputs.get(4).checkBits(bits, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean clk = clkIn.getBool();
        sel = selIn.getBool();
        if (sel) {
            addr = (int) addrIn.getValue();
            write = wnrIn.getBool();
            if (write && !lastClk && clk) {
                long data = dataIn.getValue();
                memory.setData(addr, data);
            }
        }
        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (sel && !write) {
            dataOut.set(memory.getDataWord(addr), false);
        } else {
            dataOut.setHighZ(true);
        }
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
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
    public int getBits() {
        return bits;
    }
}
