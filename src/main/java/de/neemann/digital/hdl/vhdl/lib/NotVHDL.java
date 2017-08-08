package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.vhdl.VHDLEntity;

import java.io.PrintStream;

/**
 * the not VHDL entity
 */
public class NotVHDL implements VHDLEntity {
    private boolean first = true;

    @Override
    public String getName(HDLNode node) {
        return "NOT_GATE";
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        return first;
    }

    @Override
    public void printTo(PrintStream out, HDLNode node) {
        out.print("  ");
        out.print(node.getPorts().getOutputs().get(0).getName());
        out.print(" <= ");
        out.print(" NOT( ");
        out.print(node.getPorts().getInputs().get(0).getName());
        out.println(" );");

        first = false;
    }
}
