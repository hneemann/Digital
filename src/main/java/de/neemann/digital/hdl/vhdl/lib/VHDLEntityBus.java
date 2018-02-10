package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.Separator;

import java.io.IOException;

/**
 * Handles entities which can handle multiple bit values
 */
public abstract class VHDLEntityBus extends VHDLEntitySimple {

    /**
     * Creates a new instance
     *
     * @param description the description
     */
    public VHDLEntityBus(ElementTypeDescription description) {
        super(description);
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        if (node.get(Keys.BITS) > 1) {
            out.println("generic ( bitCount : integer );");
            out.println("port (").inc();
            Separator semic = new Separator(";\n");
            for (Port p : node.getPorts()) {
                semic.check(out);
                out.print(p.getName()).print(": ").print(getDirection(p));
                if (p.getBits()>1)
                    out.print(" std_logic_vector ((bitCount-1) downto 0)");
                else
                    out.print(" std_logic");
            }
            out.println(" );").dec();
        } else
            super.writeDeclaration(out, node);
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException {
        if (node.get(Keys.BITS) > 1)
            out.print("generic map ( bitCount => ").print(node.get(Keys.BITS)).println(")");
    }

}
