package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * the not VHDL entity
 */
public class NotVHDL extends VHDLEntityBus {
    private boolean first = true;
    private boolean firstBus = true;

    /**
     * Creates a new instance
     */
    public NotVHDL() {
        super(Not.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) {
        if (node.get(Keys.BITS) > 1)
            return "NOT_GATE_BUS";
        else
            return "NOT_GATE";
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        if (node.get(Keys.BITS) > 1)
            return firstBus;
        else
            return first;
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
        out.print(node.getPorts().getOutputs().get(0).getName());
        out.print(" <= ");
        out.print(" NOT( ");
        out.print(node.getPorts().getInputs().get(0).getName());
        out.println(" );");

        if (node.get(Keys.BITS) > 1)
            firstBus = false;
        else
            first = false;
    }
}
