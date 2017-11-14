package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Driver;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * the driver VHDL entity
 */
public class DriverVHDL extends VHDLEntityBus {
    private final boolean invert;
    private boolean first = true;
    private boolean firstBus = true;

    /**
     * creates a new instance
     *
     * @param invert true if inverted input
     */
    public DriverVHDL(boolean invert) {
        super(Driver.DESCRIPTION);
        this.invert = invert;
    }

    @Override
    public String getName(HDLNode node) {
        if (node.get(Keys.BITS) > 1)
            return "DRIVER_GATE_BUS";
        else
            return "DRIVER_GATE";
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
        out.print(node.getPorts().getInputs().get(0).getName());
        out.print(" when ");
        out.print(node.getPorts().getInputs().get(1).getName());
        if (invert)
            out.print(" = '0' else ");
        else
            out.print(" = '1' else ");
        if (node.get(Keys.BITS) > 1)
            out.println("(others => 'Z');");
        else
            out.println("'Z';");


        if (node.get(Keys.BITS) > 1)
            firstBus = false;
        else
            first = false;
    }
}
