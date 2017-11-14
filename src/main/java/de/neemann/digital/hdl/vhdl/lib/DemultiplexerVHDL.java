package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Demultiplexer;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import static de.neemann.digital.hdl.vhdl.lib.MultiplexerVHDL.getBin;

/**
 * the demultiplexer VHDL entity
 */
public class DemultiplexerVHDL extends VHDLEntitySimple {
    private HashSet<Integer> first = new HashSet<>();
    private HashSet<Integer> firstBus = new HashSet<>();

    /**
     * Creates a new instance
     */
    public DemultiplexerVHDL() {
        super(Demultiplexer.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) {
        int sel = node.get(Keys.SELECTOR_BITS);
        if (node.get(Keys.BITS) > 1)
            return "DEMUX_GATE_BUS_" + sel;
        else
            return "DEMUX_GATE_" + sel;
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
            writePortGeneric(out, node.getPorts().getInputs().get(1));
            ArrayList<Port> outputs = node.getPorts().getOutputs();
            for (Port o : outputs) {
                out.println(";");
                writePortGeneric(out, o);
            }
            out.println(" );").dec();
        } else
            super.writeDeclaration(out, node);
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException {
        int sel = node.get(Keys.SELECTOR_BITS);
        out.inc();
        ArrayList<Port> outputs = node.getPorts().getOutputs();
        Port s = node.getPorts().getInputs().get(0);
        Port input = node.getPorts().getInputs().get(1);
        for (int i = 0; i < outputs.size(); i++) {
            out.print(outputs.get(i).getName());
            out.print(" <= ");
            out.print(input.getName());
            out.print(" when ");
            out.print(s.getName());
            out.print(" = ");
            out.print(getBin(i, s.getBits()));
            out.print(" else ");
            if (node.get(Keys.BITS) > 1)
                out.println("(others => '0');");
            else
                out.println("'0';");

        }
        out.dec();
        if (node.get(Keys.BITS) > 1)
            firstBus.add(sel);
        else
            first.add(sel);
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        if (node.get(Keys.BITS) > 1)
            out.print("generic map ( bitCount => ").print(node.get(Keys.BITS)).println(")");
    }
}
