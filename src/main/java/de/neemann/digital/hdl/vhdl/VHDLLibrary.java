package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.arithmetic.BitExtender;
import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
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
    private final ElementLibrary elementLibrary;
    private ArrayList<HDLNode> nodeList = new ArrayList<>();

    /**
     * Creates a new instance
     *
     * @param elementLibrary the elements library
     * @throws IOException IOException
     */
    public VHDLLibrary(ElementLibrary elementLibrary) throws IOException {
        this.elementLibrary = elementLibrary;
        map = new HashMap<>();
        put(And.DESCRIPTION, new OperateVHDL("AND", false, And.DESCRIPTION));
        put(NAnd.DESCRIPTION, new OperateVHDL("AND", true, NAnd.DESCRIPTION));
        put(Or.DESCRIPTION, new OperateVHDL("OR", false, Or.DESCRIPTION));
        put(NOr.DESCRIPTION, new OperateVHDL("OR", true, NOr.DESCRIPTION));
        put(XOr.DESCRIPTION, new OperateVHDL("XOR", false, XOr.DESCRIPTION));
        put(XNOr.DESCRIPTION, new OperateVHDL("XOR", true, XNOr.DESCRIPTION));
        put(Not.DESCRIPTION, new NotVHDL());

        put(Multiplexer.DESCRIPTION, new MultiplexerVHDL());
        put(Decoder.DESCRIPTION, new DecoderVHDL());
        put(Demultiplexer.DESCRIPTION, new DemultiplexerVHDL());
        put(BitSelector.DESCRIPTION, new BitSelectorVHDL());
        put(Driver.DESCRIPTION, new DriverVHDL(false));
        put(DriverInvSel.DESCRIPTION, new DriverVHDL(true));

        put(Comparator.DESCRIPTION, new ComparatorVHDL());
        put(BitExtender.DESCRIPTION, new BitExtenderVHDL());
        put(PriorityEncoder.DESCRIPTION, new PriorityEncoderVHDL());

        put(ROM.DESCRIPTION, new ROMVHDL());
    }

    private void put(ElementTypeDescription description, VHDLEntity entity) {
        map.put(description.getName(), entity);
    }

    private VHDLEntity getEntity(HDLNode node) throws HDLException {
        String elementName = node.getOrigName();
        VHDLEntity e = map.get(elementName);
        if (e == null) {
            try {
                ElementTypeDescription description = null;
                try {
                    description = elementLibrary.getElementType(elementName);
                } catch (ElementNotFoundException e1) {
                    // does not matter, affects only comments in the vhdl file
                }
                e = new VHDLFile(elementName, description);
                map.put(elementName, e);
            } catch (IOException e1) {
                try {
                    e1.printStackTrace();
                    LOGGER.info("could not load '" + VHDLFile.neededFileName(elementName) + "'");
                    LOGGER.info("VHDL template:\n\n" + VHDLFile.getVHDLTemplate(node));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }

        if (e == null)
            throw new HDLException(Lang.get("err_vhdlNoEntity_N", elementName));
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
        if (!nodeList.contains(node)) {
            nodeList.add(node);
            node.setHDLName(getEntity(node).getName(node));

        }
        return node.getHDLName();
    }

    private void printTo(CodePrinter out, HDLNode node) throws HDLException, IOException {
        VHDLEntity e = getEntity(node);
        if (e.needsOutput(node)) {
            out.println("\n-- " + node.getHDLName() + "\n");

            VHDLGenerator.writeComment(out, e.getDescription(node), node);

            e.writeHeader(out, node);
            out.println();
            out.println("entity " + node.getHDLName() + " is").inc();
            e.writeDeclaration(out, node);
            out.dec().println("end " + node.getHDLName() + ";\n");
            out.println("architecture " + node.getHDLName() + "_arch of " + node.getHDLName() + " is");
            if (!e.createsSignals(node))
                out.println("begin").inc();
            e.writeArchitecture(out, node);
            if (!e.createsSignals(node))
                out.dec();
            out.println("end " + node.getHDLName() + "_arch;");
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
     * @return number of nodes written
     * @throws HDLException HDLException
     * @throws IOException  IOException
     */
    public int finish(CodePrinter out) throws HDLException, IOException {
        out.println("\n-- library components");
        for (HDLNode n : nodeList)
            printTo(out, n);
        return nodeList.size();
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
