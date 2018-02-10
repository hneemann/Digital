package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.hdl.model.HDLConstant;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.util.HashSet;

/**
 * Creates the code for a ROM
 */
public class ROMVHDL extends VHDLEntitySimple {
    private HashSet<String> nameSet = new HashSet<>();

    /**
     * Creates a new instance
     */
    public ROMVHDL() {
        super(ROM.DESCRIPTION);
    }

    @Override
    public String getName(HDLNode node) throws HDLException {
        String name;
        try {
            name = "DIG_ROM_" + Port.getHDLName(node.get(Keys.LABEL));
        } catch (HDLException e) {
            throw new HDLException(Lang.get("err_vhdlRomHasNoValidLabel"));
        }
        if (nameSet.contains(name))
            throw new HDLException(Lang.get("err_vhdlRomLabel_N_notUnique", node.get(Keys.LABEL)));
        nameSet.add(name);
        return name;
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
        long[] data = node.get(Keys.DATA).getMinimized().getData();

        int dataBits = node.get(Keys.BITS);
        int addrBits = node.get(Keys.ADDR_BITS);

        out.inc().print("type mem is array ( 0 to ")
                .print(data.length - 1)
                .print(") of std_logic_vector(")
                .print(dataBits - 1)
                .println(" downto 0);");

        out.println("constant my_Rom : mem := (").inc();
        Separator sep = new Separator(", ");
        int pos = 0;
        for (int i = 0; i < data.length; i++) {
            sep.check(out);
            pos += 2;

            if (pos > 70) {
                out.println();
                pos = 0;
            }

            out.print(new HDLConstant(data[i], dataBits).vhdlValue());
            pos += 2 + dataBits;
        }
        out.dec().println(");");

        out.dec().println("begin").inc();

        out.print("process (PORT_A, PORT_sel)").eol();
        out.print("begin").inc().eol();

        out.print("if PORT_sel='0' then").inc().eol();
        out.print("PORT_D <= (others => 'Z');").dec().eol();
        if (data.length < (1 << addrBits)) {
            out.print("elsif PORT_A > ");
            out.print(new HDLConstant(data.length - 1, addrBits).vhdlValue());
            out.print(" then").inc().eol();
            out.print("PORT_D <= (others => '0');").dec().eol();
        }
        out.print("else").inc().eol();
        out.print("PORT_D <= my_rom(to_integer(unsigned(PORT_A)));").dec().eol();
        out.print("end if;").eol();

        out.dec().print("end process;").dec().eol();
    }

    @Override
    public boolean createsSignals(HDLNode node) {
        return true;
    }
}
