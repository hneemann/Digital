package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.PriorityEncoder;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The priority encoder VHDL entity.
 */
public class PriorityEncoderVHDL extends VHDLEntitySimple {
    private HashSet<Integer> first = new HashSet<>();

    /**
     * Creates a new instance
     */
    public PriorityEncoderVHDL() {
        super(PriorityEncoder.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        return "PRIORITY_GATE_" + sel;
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

        out.print(node.getPorts().getOutputs().get(0).getName()).println(" <=").inc();
        for (int i = inputs.size() - 1; i >= 1; i--)
            out.print(getBin(i, sel)).print(" when ").print(inputs.get(i).getName()).println(" = '1' else");

        out.print(getBin(0, sel)).println(" ;");

        out.dec();

        out.print(node.getPorts().getOutputs().get(1).getName()).print(" <= ");
        for (int i = 0; i < inputs.size(); i++) {
            if (i > 0)
                out.print(" OR ");
            out.print(inputs.get(i).getName());
        }
        out.println(";");


        first.add(sel);
    }

    /**
     * Returns the given value as a VHDL string
     *
     * @param val  the value
     * @param bits the number of bits
     * @return the string representation
     */
    public static String getBin(int val, int bits) {
        String s = Integer.toBinaryString(val);
        while (s.length() < bits)
            s = "0" + s;

        if (bits > 1)
            s = "\"" + s + "\"";
        else
            s = "'" + s + "'";

        return s;
    }

}
