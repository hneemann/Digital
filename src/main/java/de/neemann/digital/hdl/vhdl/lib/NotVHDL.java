package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.vhdl.VHDLEntity;

import java.io.PrintStream;

/**
 * the not VHDL entity
 */
public class NotVHDL implements VHDLEntity {
    private boolean first = true;
    private boolean firstBus = true;

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
    public void printTo(PrintStream out, HDLNode node) {
        out.print("  ");
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

    @Override
    public boolean hasGenerics(HDLNode node) {
        return node.get(Keys.BITS) > 1;
    }

    @Override
    public void writeGenerics(PrintStream out, HDLNode node) {
        out.println("  generic ( bitCount : integer );");
    }

    @Override
    public void writeGenericPorts(PrintStream out, HDLNode node) {
        out.println("  port ( ");
        out.println("    PORT_out: out std_logic_vector ((bitCount-1) downto 0);");
        out.println("    PORT_in: in std_logic_vector ((bitCount-1) downto 0) );");
    }

    @Override
    public void writeGenericMap(PrintStream out, HDLNode node) {
        out.println("    generic map ( bitCount => "+node.get(Keys.BITS)+")");
    }
}
