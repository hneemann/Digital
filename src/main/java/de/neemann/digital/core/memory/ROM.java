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
public class ROM extends Node implements Element {


    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(ROM.class, "A", "sel")
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.AddrBits)
            .addAttribute(AttributeKey.Data);

    private final DataField data;
    private final ObservableValue output;
    private final int addrBits;
    private ObservableValue addrIn;
    private ObservableValue selIn;
    private int addr;
    private boolean sel;

    public ROM(ElementAttributes attr) {
        int bits = attr.get(AttributeKey.Bits);
        output = new ObservableValue("D", bits, true);
        data = attr.get(AttributeKey.Data);
        addrBits = attr.get(AttributeKey.AddrBits);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserver(this);
        selIn = inputs[1].checkBits(1, this).addObserver(this);
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void readInputs() throws NodeException {
        addr = (int) addrIn.getValue();
        sel = selIn.getBool();
    }

    @Override
    public void writeOutputs() throws NodeException {
        output.set(data.getData(addr), !sel);
    }

}
