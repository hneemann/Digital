package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.wiring.Decoder;
import de.neemann.digital.core.wiring.Multiplexer;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl.lib.*;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The library of VHDL entities
 */
public class VHDLLibrary {

    private static final Logger LOGGER = LoggerFactory.getLogger(VHDLLibrary.class);
    private final HashMap<String, VHDLEntity> map;
    private ArrayList<HDLNode> nodeList = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @throws IOException IOException
     */
    public VHDLLibrary() throws IOException {
        map = new HashMap<>();
        map.put(And.DESCRIPTION.getName(), new OperateVHDL("AND", false));
        map.put(NAnd.DESCRIPTION.getName(), new OperateVHDL("AND", true));
        map.put(Or.DESCRIPTION.getName(), new OperateVHDL("OR", false));
        map.put(NOr.DESCRIPTION.getName(), new OperateVHDL("OR", true));
        map.put(XOr.DESCRIPTION.getName(), new OperateVHDL("XOR", false));
        map.put(XNOr.DESCRIPTION.getName(), new OperateVHDL("XOR", true));
        map.put(Not.DESCRIPTION.getName(), new NotVHDL());

        map.put(Multiplexer.DESCRIPTION.getName(), new MultiplexerVHDL());
        map.put(Decoder.DESCRIPTION.getName(), new DecoderVHDL());
    }

    private VHDLEntity getEntity(HDLNode node) throws HDLException {
        String elementName = node.getName();
        VHDLEntity e = map.get(elementName);
        if (e == null) {
            try {
                e = new VHDLFile(elementName);
                map.put(elementName, e);
            } catch (IOException e1) {
                e1.printStackTrace();
                try {
                    LOGGER.info("could not load '" + VHDLFile.neededFileName(elementName) + "'");
                    LOGGER.info("VHDL template:\n\n" + VHDLFile.getVHDLTemplate(node));
                    LOGGER.info("You should replace the types for the data with '{{data}}'");
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (e == null)
            throw new HDLException(Lang.get("err_noVhdlEntity_N", elementName));
        return e;
    }

    /**
     * Returns the vhdl name of the given node
     *
     * @param node the node
     * @return the name
     * @throws HDLException HDLException
     */
    public String getName(HDLNode node) throws HDLException {
        if (!nodeList.contains(node))
            nodeList.add(node);
        return getEntity(node).getName(node);
    }

    private void printTo(CodePrinter out, HDLNode node) throws HDLException, IOException {
        VHDLEntity e = getEntity(node);
        if (e.needsOutput(node)) {
            out.println("\n-- " + e.getName(node) + "\n");
            e.writeHeader(out, node);
            out.println();
            out.println("entity " + e.getName(node) + " is").inc();
            e.writeDeclaration(out, node);
            out.dec().println("end " + e.getName(node) + ";\n");
            out.println("architecture " + e.getName(node) + "_arch of " + e.getName(node) + " is");
            if (!e.createsSignals())
                out.println("begin").inc();
            e.writeArchitecture(out, node);
            if (!e.createsSignals())
                out.dec();
            out.println("end " + e.getName(node) + "_arch;");
        }
    }

    /**
     * Writes the ports to the file
     *
     * @param out  the output stream
     * @param node the node
     * @throws HDLException HDLException
     * @throws IOException  IOException
     */
    public void writeDeclaration(CodePrinter out, HDLNode node) throws HDLException, IOException {
        if (node.isCustom()) {
            out.println("port (").inc();
            Separator semic = new Separator(";\n");
            for (Port p : node.getPorts()) {
                semic.check(out);
                VHDLEntitySimple.writePort(out, p);
            }
            out.println(" );").dec();
        } else {
            VHDLEntity e = getEntity(node);
            e.writeDeclaration(out, node);
        }
    }

    /**
     * Adds all used library components to the vhdl file
     *
     * @param out the pront stream
     * @throws HDLException HDLException
     * @throws IOException  IOException
     */
    public void finish(CodePrinter out) throws HDLException, IOException {
        out.println("\n-- library components");
        for (HDLNode n : nodeList)
            printTo(out, n);
    }

    /**
     * Writes the generic map to the given stream
     *
     * @param out  the output stream
     * @param node the node
     * @throws HDLException HDLException
     * @throws IOException  IOException
     */
    public void writeGenericMap(CodePrinter out, HDLNode node) throws HDLException, IOException {
        if (!node.isCustom()) {
            VHDLEntity e = getEntity(node);
            e.writeGenericMap(out, node);
        }
    }
}
