package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.BitSelector;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static de.neemann.digital.hdl.vhdl.lib.MultiplexerVHDL.getBin;

/**
 * The BitSelector VHDL entity
 */
public class BitSelectorVHDL extends VHDLEntitySimple {
    private HashSet<Integer> first = new HashSet<>();

    /**
     * Creates a new instance
     */
    public BitSelectorVHDL() {
        super(BitSelector.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        return "BIT_SEL_" + sel;
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        return !first.contains(sel);
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
        int sel = node.get(Keys.SELECTOR_BITS);
        ArrayList<Port> inputs = node.getPorts().getInputs();
        out.println("with " + inputs.get(1).getName() + " select").inc();

        out.print(node.getPorts().getOutputs().get(0).getName()).println(" <=").inc();
        int inBits = 1 << sel;
        for (int i = 0; i < inBits; i++)
            out.print(inputs.get(0).getName()).print("(").print(i).print(") when ").print(getBin(i, sel)).println(",");

        out.println("'0' when others;");

        out.dec().dec();

        first.add(sel);
    }

}
