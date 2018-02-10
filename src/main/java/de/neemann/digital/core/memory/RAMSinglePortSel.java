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
            input("CS"),
            input("WE").setClock(),
            input("OE"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERTER_CONFIG);

    private final int bits;
    private final int addrBits;
    private final int size;
    private final DataField memory;
    private final String label;
    private final ObservableValue dataOut;
    private ObservableValue addrIn;
    private ObservableValue csIn;
    private ObservableValue weIn;
    private ObservableValue oeIn;
    private ObservableValue dataIn;

    private boolean cs;
    private int addr;
    private boolean oe;
    private boolean lastweIn;

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
        memory = createDataField(attr, size);
        label = attr.getCleanLabel();
        dataOut = new ObservableValue("D", bits, true).setPinDescription(DESCRIPTION).setBidirectional();
    }

    /**
     * creates the data field to use
     *
     * @param attr the elements attributes
     * @param size the size of the memory
     * @return the memory to use
     */
    protected DataField createDataField(ElementAttributes attr, int size) {
        return new DataField(size);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        addrIn = inputs.get(0).checkBits(addrBits, this).addObserverToValue(this);
        csIn = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        weIn = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        oeIn = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        dataIn = inputs.get(4).checkBits(bits, this);
    }

    @Override
    public void readInputs() throws NodeException {
        final boolean weIn = this.weIn.getBool();
        cs = csIn.getBool();
        if (cs) {
            addr = (int) addrIn.getValue();
            oe = oeIn.getBool();
            if (weIn && !lastweIn) {
                long data = dataIn.getValue();
                memory.setData(addr, data);
            }
        }
        lastweIn=weIn;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (cs && oe) {
            dataOut.set(memory.getDataWord(addr), false);
        } else {
            dataOut.set(0, true);
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
    public int getDataBits() {
        return bits;
    }

    @Override
    public int getAddrBits() {
        return addrBits;
    }
}
