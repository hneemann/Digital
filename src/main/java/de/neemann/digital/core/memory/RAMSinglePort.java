/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * RAM module with a single port to read and write data.
 */
public class RAMSinglePort extends RAMDualPort {

    /**
     * The RAMs {@link ElementTypeDescription}
     */

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RAMSinglePort.class,
            input("A"),
            input("str"),
            input("C").setClock(),
            input("ld"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.ADDR_BITS)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.IS_PROGRAM_MEMORY)
            .addAttribute(Keys.LABEL);

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
                .setBidirectional()
                .setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        setAddrIn(inputs.get(0));
        setStrIn(inputs.get(1));
        setClkIn(inputs.get(2));
        setLdIn(inputs.get(3));
        setDataIn(inputs.get(4)); // additional input to read the port
    }

}
