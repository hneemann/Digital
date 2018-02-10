package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * the Comparator VHDL entity
 */
public class ComparatorVHDL extends VHDLEntityBus {
    private boolean first = true;
    private boolean firstSigned = true;

    /**
     * Creates a new instance
     */
    public ComparatorVHDL() {
        super(Comparator.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        if (node.get(Keys.BITS) == 1)
            throw new HDLException("comparator with one bit is not supported!");

        if (node.get(Keys.SIGNED))
            return "COMP_GATE_SIGNED";
        else
            return "COMP_GATE_UNSIGNED";
    }

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        super.writeHeader(out, node);
        if (node.get(Keys.SIGNED))
            out.println("USE ieee.numeric_std.all;");
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        if (node.get(Keys.SIGNED))
            return firstSigned;
        else
            return first;
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
        if (node.get(Keys.SIGNED))
            out
                    .println("process(PORT_a,PORT_b)")
                    .println("begin")
                    .println("  if (signed(PORT_a) > signed(PORT_b)) then")
                    .println("    PORT_le <= '0';")
                    .println("    PORT_eq <= '0';")
                    .println("    PORT_gr <= '1';")
                    .println("  elsif (signed(PORT_a) < signed(PORT_b)) then")
                    .println("    PORT_le <= '1';")
                    .println("    PORT_eq <= '0';")
                    .println("    PORT_gr <= '0';")
                    .println("  else")
                    .println("    PORT_le <= '0';")
                    .println("    PORT_eq <= '1';")
                    .println("    PORT_gr <= '0';")
                    .println("  end if;")
                    .println("end process;");
        else
            out
                    .println("process(PORT_a,PORT_b)")
                    .println("begin")
                    .println("  if (PORT_a > PORT_b ) then")
                    .println("    PORT_le <= '0';")
                    .println("    PORT_eq <= '0';")
                    .println("    PORT_gr <= '1';")
                    .println("  elsif (PORT_a < PORT_b) then")
                    .println("    PORT_le <= '1';")
                    .println("    PORT_eq <= '0';")
                    .println("    PORT_gr <= '0';")
                    .println("  else")
                    .println("    PORT_le <= '0';")
                    .println("    PORT_eq <= '1';")
                    .println("    PORT_gr <= '0';")
                    .println("  end if;")
                    .println("end process;");

        if (node.get(Keys.SIGNED))
            firstSigned = false;
        else
            first = false;
    }
}
