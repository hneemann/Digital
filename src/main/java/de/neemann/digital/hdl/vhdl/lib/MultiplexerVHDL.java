package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Multiplexer;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * the multiplexer VHDL entity
 */
public class MultiplexerVHDL extends VHDLEntitySimple {
    private HashSet<Integer> first = new HashSet<>();
    private HashSet<Integer> firstBus = new HashSet<>();

    /**
     * Creates a new instance
     */
    public MultiplexerVHDL() {
        super(Multiplexer.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        if (node.get(Keys.BITS) > 1)
            return "MUX_GATE_BUS_" + sel;
        else
            return "MUX_GATE_" + sel;
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        if (node.get(Keys.BITS) > 1)
            return !firstBus.contains(sel);
        else
            return !first.contains(sel);
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        if (node.get(Keys.BITS) > 1) {
            out.println("generic ( bitCount : integer );");
            out.println("port (").inc();
            ArrayList<Port> inputs = node.getPorts().getInputs();
            writePort(out, inputs.get(0));
            out.println(";");
            writePortGeneric(out, node.getPorts().getOutputs().get(0));
            for (int i = 1; i < inputs.size(); i++) {
                out.println(";");
                writePortGeneric(out, inputs.get(i));
            }

            out.println(" );").dec();

        } else
            super.writeDeclaration(out, node);
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
        int sel = node.get(Keys.SELECTOR_BITS);
        ArrayList<Port> inputs = node.getPorts().getInputs();
        out.println("with " + inputs.get(0).getName() + " select").inc();

        out.print(node.getPorts().getOutputs().get(0).getName()).println(" <=").inc();
        for (int i = 1; i < inputs.size(); i++)
            out.print(inputs.get(i).getName()).print(" when ").print(getBin(i - 1, sel)).println(",");

        if (node.get(Keys.BITS) == 1)
            out.println("'0' when others;");
        else
            out.println("(others => '0') when others;");

        out.dec().dec();

        if (node.get(Keys.BITS) > 1)
            firstBus.add(sel);
        else
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

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        if (node.get(Keys.BITS) > 1)
            out.print("generic map ( bitCount => ").print(node.get(Keys.BITS)).println(")");
    }
}
