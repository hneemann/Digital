package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The decoder VHDL entity
 */
public class DecoderVHDL extends VHDLEntitySimple {
    private HashSet<Integer> written = new HashSet<>();

    /**
     * Creates a new instance
     */
    public DecoderVHDL() {
        super(Decoder.DESCRIPTION);
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        return !written.contains(node.get(Keys.SELECTOR_BITS));
    }

    @Override
    public String getName(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        return "MUX_GATE_" + sel;
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        int sel = node.get(Keys.SELECTOR_BITS);
        ArrayList<Port> outputs = node.getPorts().getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            out.print(outputs.get(i).getName());
            out.print(" <= '1' when ");
            out.print(node.getPorts().getInputs().get(0).getName());
            out.print(" = ");
            out.print(MultiplexerVHDL.getBin(i, sel));
            out.println(" else '0';");
        }

        written.add(node.get(Keys.SELECTOR_BITS));
    }
}
