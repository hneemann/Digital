package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.arithmetic.BitExtender;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLNode;

import java.io.IOException;

/**
 * VHDLEntity to create the BitExtenders VHDL code
 */
public class BitExtenderVHDL extends VHDLEntityDelegate {
    private final VHDLFile delegate;
    private final VHDLFile delegateSingleBit;

    /**
     * Creates a new instance
     *
     * @throws IOException IOException
     */
    public BitExtenderVHDL() throws IOException {
        delegate = new VHDLFile("BitExtender", BitExtender.DESCRIPTION);
        delegateSingleBit = new VHDLFile("BitExtenderSingle", BitExtender.DESCRIPTION);
    }

    @Override
    public VHDLEntity getDelegate(HDLNode node) {
        if (node.get(Keys.INPUT_BITS) == 1)
            return delegateSingleBit;
        else
            return delegate;
    }
}
