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
 * A EEPROM module.
 *
 * @author hneemann
 */
public class EEPROM extends Node implements Element {

    /**
     * The EEPROMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(EEPROM.class,
            input("A"),
            input("CS"),
            input("WE"),
            input("OE"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DATA);

    private final int bits;
    private final int addrBits;
    private final DataField memory;
    private final ObservableValue dataOut;

    private ObservableValue addrIn;
    private ObservableValue csIn;
    private ObservableValue weIn;
    private ObservableValue oeIn;
    private ObservableValue dataIn;
    private int addr;
    private boolean cs;
    private boolean oe;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public EEPROM(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        addrBits = attr.get(Keys.ADDR_BITS);
        int size = 1 << addrBits;
        DataField memory = attr.get(Keys.DATA);
        if (memory.size() < size) {
            memory = new DataField(memory, size);
            attr.set(Keys.DATA, memory);
        }
        this.memory = memory;
        dataOut = new ObservableValue("D", bits, true).setPinDescription(DESCRIPTION).setBidirectional();
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
            if (weIn.getBool()) {
                long data = dataIn.getValue();
                memory.setData(addr, data);
            }
        }
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
    public ObservableValues getOutputs() {
        return dataOut.asList();
    }

}
