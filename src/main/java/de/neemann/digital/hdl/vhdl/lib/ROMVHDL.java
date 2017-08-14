package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.hdl.vhdl.VHDLExporter;

import java.io.IOException;

/**
 * Creates the code for a ROM
 */
public class ROMVHDL extends VHDLEntitySimple {
    @Override
    public String getName(HDLNode node) throws HDLException {
        try {
            return "DIG_ROM_" + Port.getHDLName(node.get(Keys.LABEL));
        } catch (HDLException e) {
            throw new HDLException("Rom has no valid label");
        }
    }

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        super.writeHeader(out, node);
        out.println("use IEEE.NUMERIC_STD.ALL;");
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        return true;
    }

    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        DataField data = node.get(Keys.DATA).getMinimized();

        Integer dataBits = node.get(Keys.BITS);
        out.inc().print("type mem is array ( 0 to ")
                .print(data.size()-1)
                .print(") of std_logic_vector(")
                .print(dataBits - 1)
                .println(" downto 0);");

        out.println("constant my_Rom : mem := (").inc();
        Separator sep = new Separator(",\n");
        for (int i = 0; i < data.size(); i++) {
            sep.check(out);
            VHDLExporter.writeValue(out, data.getDataWord(i), dataBits);
        }
        out.dec().println(");");

        out.dec().println("begin").inc();

        out.print("PORT_D <= my_rom(to_integer(unsigned(PORT_A))) when PORT_sel='1' else (others => 'Z');");
    }

    @Override
    public boolean createsSignals() {
        return true;
    }
}
