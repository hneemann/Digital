package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.lang.Lang;

import java.io.IOException;

/**
 * A generic vhdl entity.
 */
public abstract class VHDLEntitySimple implements VHDLEntity {
    private boolean first = true;

    @Override
    public boolean needsOutput(HDLNode node) {
        if (first) {
            first = false;
            return true;
        } else
            return false;
    }

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        out.println("LIBRARY ieee;");
        out.println("USE ieee.std_logic_1164.all;\n");
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        out.println("port (").inc();
        Separator semic = new Separator(";\n");
        for (Port p : node.getPorts()) {
            semic.check(out);
            out.print(p.getName()).print(": ").print(getDirection(p)).print(" ").print(getType(p.getBits()));
        }
        out.println(" );").dec();
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
    }

    /**
     * Returns the VHDL direction qualifier
     *
     * @param p the port
     * @return the direction
     * @throws HDLException HDLException
     */
    public static String getDirection(Port p) throws HDLException {
        switch (p.getDirection()) {
            case in:
                return "in";
            case out:
                return "out";
            default:
                throw new HDLException(Lang.get("err_vhdlUnknownPortType_N", p.getDirection().toString()));
        }
    }

    /**
     * returns the vhdl type
     *
     * @param bits the number of bits
     * @return the type
     * @throws HDLException HDLException
     */
    public static String getType(int bits) throws HDLException {
        if (bits == 0)
            throw new HDLException("bit number not available");
        if (bits == 1)
            return "std_logic";
        else
            return "std_logic_vector (" + (bits - 1) + " downto 0)";
    }

}
