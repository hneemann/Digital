package de.neemann.digital.core.memory;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class RAMSinglePort extends RAMDualPort {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMSinglePort.class, "A", "str", "c", "ld")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.AddrBits)
            .addAttribute(AttributeKey.Label)
            .setShortName("RAM");

    public RAMSinglePort(ElementAttributes attr) {
        super(attr);
    }

    /**
     * If a port is bidirectional an additional input comes with <code>setInputs</code>.
     * Using this input you can read back the output port.
     *
     * @return outputs
     */
    @Override
    protected ObservableValue createOutput() {
        return super.createOutput().setBidirectional(true);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserver(this);
        strIn = inputs[1].checkBits(1, this).addObserver(this);
        clkIn = inputs[2].checkBits(1, this).addObserver(this);
        ldIn = inputs[3].checkBits(1, this).addObserver(this);
        dataIn = inputs[4].checkBits(bits, this).addObserver(this); // additional input to read the port
    }

}
