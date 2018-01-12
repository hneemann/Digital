package de.neemann.digital.core.memory;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with 2 different ports to read and 1 port to write the data.
 *
 * @author david@summersoft.fay-ar.us
 */
public class RAMTripleAccess extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMTripleAccess.class,
            input("str"),
            input("C"),
            input("1A"),
            input("2A"),
            input("D_in"),
            input("A"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.LABEL);

    private final DataField memory;
    private final ObservableValue out1;
    private final ObservableValue out2;
    private final int addrBits;
    private final int bits;
    private final String label;
    private final int size;
    private ObservableValue addr1In;
    private ObservableValue addr2In;
    private ObservableValue addrIn;
    private ObservableValue dataIn;
    private ObservableValue str1In;
    private ObservableValue clk1In;
    private int addr1;
    private int addr2;
    private int addr;
    private boolean lastClk = false;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public RAMTripleAccess(ElementAttributes attr) {
        super(true);
        bits = attr.get(Keys.BITS);
        out1 = new ObservableValue("1D", bits, true).setPinDescription(DESCRIPTION);
        out2 = new ObservableValue("2D", bits).setPinDescription(DESCRIPTION);
        addrBits = attr.get(Keys.ADDR_BITS);
        size = 1 << addrBits;
        memory = new DataField(size);
        label = attr.getCleanLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        this.str1In  = inputs.get(0).checkBits(1, this);
        this.clk1In  = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        this.addr1In = inputs.get(2).checkBits(addrBits, this).addObserverToValue(this);
        this.addr2In = inputs.get(3).checkBits(addrBits, this).addObserverToValue(this);
        this.dataIn  = inputs.get(4).checkBits(bits, this);
        this.addrIn  = inputs.get(5).checkBits(addrBits, this).addObserverToValue(this);
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(out1, out2);
    }

    @Override
    public void readInputs() throws NodeException {
        long data = 0;
        boolean clk = clk1In.getBool();
        boolean str;
        if (!lastClk && clk) {
            str = str1In.getBool();
            if (str)
                data = dataIn.getValue();
        } else
            str = false;
        if (str)
            addr = (int) addrIn.getValue();
        if (str)
            memory.setData(addr, data);

        addr1 = (int) addr1In.getValue();
        addr2 = (int) addr2In.getValue();

        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        out1.setValue(memory.getDataWord(addr1));
        out2.setValue(memory.getDataWord(addr2));
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
