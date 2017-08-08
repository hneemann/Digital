package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.hdl.vhdl.VHDLEntity;
import de.neemann.digital.hdl.vhdl.VHDLExporter;

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
        if (node.get(Keys.BITS) > 1)
            return op + "_GATE_BUS_" + node.get(Keys.INPUT_COUNT);
        else
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

    @Override
    public boolean hasGenerics(HDLNode node) {
        return node.get(Keys.BITS) > 1;
    }

    @Override
    public void writeGenerics(PrintStream out, HDLNode node) throws HDLException {
        out.println("  generic ( bitCount : integer );");
    }

    @Override
    public void writeGenericPorts(PrintStream out, HDLNode node) throws HDLException {
        out.println("  port (");
        Separator semic = new Separator(";\n");
        for (Port p : node.getPorts()) {
            semic.check(out);
            out.print("    " + p.getName() + ": " + VHDLExporter.getDirection(p) + " std_logic_vector ((bitCount-1) downto 0)");
        }
        out.println(" );");
    }

    @Override
    public void writeGenericMap(PrintStream out, HDLNode node) {
        out.println("    generic map ( bitCount => "+node.get(Keys.BITS)+")");
    }
}
