package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.hdl.vhdl.VHDLEntity;

import java.io.PrintStream;
import java.util.HashSet;

/**
 * A vhdl operation, used to create AND, NAND, OR and NOR.
 */
public class OperateVHDL implements VHDLEntity {
    private String op;
    private boolean invert;
    private HashSet<Integer> inputsWritten = new HashSet<>();

    /**
     * Creates a new instance
     *
     * @param op     the operator
     * @param invert true if inverted output
     */
    public OperateVHDL(String op, boolean invert) {
        this.op = op;
        this.invert = invert;
    }

    @Override
    public String getName(HDLNode node) {
        return op + "_GATE_" + node.get(Keys.INPUT_COUNT);
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        return !inputsWritten.contains(node.get(Keys.INPUT_COUNT));
    }

    @Override
    public void printTo(PrintStream out, HDLNode node) {
        out.print("  ");
        out.print(node.getPorts().getOutputs().get(0).getName());
        out.print(" <= ");
        if (invert) out.print(" NOT( ");
        Separator and = new Separator(" " + op + " ");
        for (Port p : node.getPorts().getInputs()) {
            and.check(out);
            out.print(p.getName());
        }
        if (invert) out.print(" )");
        out.println(";");

        inputsWritten.add(node.get(Keys.INPUT_COUNT));
    }
}
