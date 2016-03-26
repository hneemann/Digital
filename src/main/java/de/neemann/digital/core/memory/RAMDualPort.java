package de.neemann.digital.core.memory;

import de.neemann.digital.core.Node;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class RAMDualPort extends Node implements Element {


    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMDualPort.class, "A", "D", "str", "c", "ld")
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.AddrBits);

    private final DataField memory;
    private final ObservableValue output;
    private final int addrBits;
    private final int bits;
    private ObservableValue addrIn;
    private ObservableValue dataIn;
    private ObservableValue strIn;
    private ObservableValue clkIn;
    private ObservableValue ldIn;
    private int addr;
    private boolean lastClk = false;
    private boolean ld;

    public RAMDualPort(ElementAttributes attr) {
        bits = attr.get(AttributeKey.Bits);
        output = new ObservableValue("D", bits, true);
        addrBits = attr.get(AttributeKey.AddrBits);
        memory = new DataField(1 << addrBits);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserver(this);
        dataIn = inputs[1].checkBits(bits, this).addObserver(this);
        strIn = inputs[2].checkBits(1, this).addObserver(this);
        clkIn = inputs[3].checkBits(1, this).addObserver(this);
        ldIn = inputs[4].checkBits(1, this).addObserver(this);
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

}
