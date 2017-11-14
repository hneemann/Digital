package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;

import java.io.IOException;
import java.util.HashSet;

/**
 * A vhdl operation, used to create AND, NAND, OR and NOR.
 */
public class OperateVHDL extends VHDLEntityBus {
    private String op;
    private boolean invert;
    private HashSet<Integer> inputsWritten = new HashSet<>();
    private HashSet<Integer> inputsWrittenBus = new HashSet<>();

    /**
     * Creates a new instance
     *
     * @param op          the operator
     * @param invert      true if inverted output
     * @param description the elements description
     */
    public OperateVHDL(String op, boolean invert, ElementTypeDescription description) {
        super(description);
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
        if (node.get(Keys.BITS) > 1)
            return !inputsWrittenBus.contains(node.get(Keys.INPUT_COUNT));
        else
            return !inputsWritten.contains(node.get(Keys.INPUT_COUNT));
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
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

        if (node.get(Keys.BITS) > 1)
            inputsWrittenBus.add(node.get(Keys.INPUT_COUNT));
        else
            inputsWritten.add(node.get(Keys.INPUT_COUNT));
    }
}
