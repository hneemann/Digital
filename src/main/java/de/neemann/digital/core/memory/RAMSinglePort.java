package de.neemann.digital.core.memory;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with a single port to read and write data.
 *
 * @author hneemann
 */
public class RAMSinglePort extends RAMDualPort {

    /**
     * The RAMs {@link ElementTypeDescription}
     */

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMSinglePort.class,
            input("A", Lang.get("elem_RAMSinglePort_pin_addr")),
            input("str", Lang.get("elem_RAMSinglePort_pin_str")),
            input("c", Lang.get("elem_RAMSinglePort_pin_c")),
            input("ld", Lang.get("elem_RAMSinglePort_pin_ld")))
            .addAttribute(Keys.Rotate)
            .addAttribute(Keys.Bits)
            .addAttribute(Keys.AddrBits)
            .addAttribute(Keys.Label)
            .setShortName("RAM");

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
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
        return super.createOutput()
                .setBidirectional(true)
                .setDescription(Lang.get("elem_RAMSinglePort_pin_d"));
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        addrIn = inputs[0].checkBits(addrBits, this).addObserverToValue(this);
        strIn = inputs[1].checkBits(1, this).addObserverToValue(this);
        clkIn = inputs[2].checkBits(1, this).addObserverToValue(this);
        ldIn = inputs[3].checkBits(1, this).addObserverToValue(this);
        dataIn = inputs[4].checkBits(bits, this).addObserverToValue(this); // additional input to read the port
    }

}
