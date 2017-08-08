package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.basic.*;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.vhdl.lib.NotVHDL;
import de.neemann.digital.hdl.vhdl.lib.OperateVHDL;
import de.neemann.digital.lang.Lang;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

/**
 * The library of VHDL entities
 */
public class VHDLLibrary {

    private final HashMap<String, VHDLEntity> map;
    private HashSet<HDLNode> nodeset = new HashSet<>();

    /**
     * Creates a new instance
     */
    public VHDLLibrary() {
        map = new HashMap<>();
        map.put(And.DESCRIPTION.getName(), new OperateVHDL("AND", false));
        map.put(NAnd.DESCRIPTION.getName(), new OperateVHDL("AND", true));
        map.put(Or.DESCRIPTION.getName(), new OperateVHDL("OR", false));
        map.put(NOr.DESCRIPTION.getName(), new OperateVHDL("OR", true));
        map.put(Not.DESCRIPTION.getName(), new NotVHDL());
    }

    private VHDLEntity getEntity(HDLNode node) throws HDLException {
        VHDLEntity e = map.get(node.getVisualElement().getElementName());
        if (e == null)
            throw new HDLException(Lang.get("err_noVhdlEntity_N", node.getVisualElement().getElementName()));
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
        nodeset.add(node);
        return getEntity(node).getName(node);
    }

    private void printTo(PrintStream out, HDLNode node) throws HDLException {
        VHDLEntity e = getEntity(node);
        if (e.needsOutput(node)) {
            out.println("\n-- " + e.getName(node) + "\n");
            VHDLExporter.writeHeader(out);
            out.println("entity " + e.getName(node) + " is");
            VHDLExporter.writePort(out, "  ", node.getPorts());
            out.println("end " + e.getName(node) + ";\n");
            out.println("architecture " + e.getName(node) + "_arch of " + e.getName(node) + " is");
            out.println("begin");
            e.printTo(out, node);
            out.println("end " + e.getName(node) + "_arch;");
        }
    }

    /**
     * Adds all used library components to the vhdl file
     *
     * @param out the pront stream
     * @throws HDLException HDLException
     */
    public void finish(PrintStream out) throws HDLException {
        out.println("\n-- library components");
        for (HDLNode n : nodeset)
            printTo(out, n);
    }
}
