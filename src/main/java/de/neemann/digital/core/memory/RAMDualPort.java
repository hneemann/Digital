package de.neemann.digital.core.memory;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with different ports to read and write the data.
 *
 * @author hneemann
 */
public class RAMDualPort extends Node implements Element, RAMInterface {

    /**
     * The RAMs {@link ElementTypeDescription}
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMDualPort.class,
            input("A", Lang.get("elem_RAMDualPort_pin_addr")),
            input("D", Lang.get("elem_RAMDualPort_pin_dataIn")),
            input("str", Lang.get("elem_RAMDualPort_pin_str")),
            input("c", Lang.get("elem_RAMDualPort_pin_c")),
            input("ld", Lang.get("elem_RAMDualPort_pin_ld")))
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.Bits)
            .addAttribute(Keys.AddrBits)
            .addAttribute(Keys.Label)
            .setShortName("RAM");

    private final DataField memory;
    private final ObservableValue output;
    protected final int addrBits;
    protected final int bits;
    protected ObservableValue addrIn;
    protected ObservableValue dataIn;
    protected ObservableValue strIn;
    protected ObservableValue clkIn;
    protected ObservableValue ldIn;
    private int addr;
    private boolean lastClk = false;
    private boolean ld;

    /**
     * Creates a new instance
     *
     * @param attr the elemets attributes
     */
    public RAMDualPort(ElementAttributes attr) {
        bits = attr.get(Keys.Bits);
        output = createOutput();
        addrBits = attr.get(Keys.AddrBits);
        memory = new DataField(1 << addrBits, bits);
    }

    /**
     * called to create the output value
     *
     * @return the output value
     */
    protected ObservableValue createOutput() {
        return new ObservableValue("D", bits, true).setDescription(Lang.get("elem_RAMDualPort_pin_dataOut"));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserverToValue(this);
        dataIn = inputs[1].checkBits(bits, this).addObserverToValue(this);
        strIn = inputs[2].checkBits(1, this).addObserverToValue(this);
        clkIn = inputs[3].checkBits(1, this).addObserverToValue(this);
        ldIn = inputs[4].checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void readInputs() throws NodeException {
        long data = 0;
        boolean clk = clkIn.getBool();
        boolean str;
        if (!lastClk && clk) {
            str = strIn.getBool();
            if (str)
                data = dataIn.getValue();
        } else
            str = false;
        ld = ldIn.getBool();
        if (ld || str)
            addr = (int) addrIn.getValue();

        if (str)
            memory.setData(addr, data);

        lastClk = clk;
    }

    @Override
    public void writeOutputs() throws NodeException {
        if (ld) {
            output.set(memory.getData(addr), false);
        } else {
            output.setHighZ(true);
        }
    }

    @Override
    public DataField getMemory() {
        return memory;
    }
}
