package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.*;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.HashSet;

/**
 * Exports the given circuit to vhdl
 */
public class VHDLExporter implements Closeable {

    private final StringWriter stringWriter;
    private final BufferedWriter out;
    private final ElementLibrary library;

    /**
     * Creates a new exporter
     *
     * @param library the library
     */
    public VHDLExporter(ElementLibrary library) {
        this(library, new StringWriter());
    }

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @param out     the output stream
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    public VHDLExporter(ElementLibrary library, OutputStream out) throws UnsupportedEncodingException {
        this(library, new OutputStreamWriter(out, "utf-8"));
    }

    /**
     * Creates a new exporter
     *
     * @param library the library
     * @param out     the writer
     */
    public VHDLExporter(ElementLibrary library, Writer out) {
        this.library = library;
        if (out instanceof StringWriter)
            stringWriter = (StringWriter) out;
        else
            stringWriter = null;

        if (out instanceof BufferedWriter)
            this.out = (BufferedWriter) out;
        else
            this.out = new BufferedWriter(out);
    }

    /**
     * Writes the file to the given stream
     *
     * @param circuit the circuit to export
     * @return this for chained calls
     * @throws IOException IOException
     */
    public VHDLExporter export(Circuit circuit) throws IOException {
        try {
            HDLModel model = new HDLModel(circuit, library);
            export(model, "main");
        } catch (PinException | HDLException | ElementNotFoundException | NodeException e) {
            throw new IOException(Lang.get("err_exporting_vhdl"), e);
        }
        return this;
    }

    private void export(HDLModel model, String name) throws IOException, PinException, HDLException, ElementNotFoundException, NodeException {
        out.write("entity " + name + " is\n");
        writePort("  ", model.getPorts());
        out.write("end " + name + ";\n");

        out.write("\narchitecture " + name + "_arch of " + name + " is\n");

        HashSet<String> componentsWritten = new HashSet<>();
        for (HDLNode node : model) {
            String nodeName = getVhdlEntityName(node);
            if (!componentsWritten.contains(nodeName)) {
                writeComponent(node);
                componentsWritten.add(nodeName);
            }
        }
        out.write("\n");
        for (Signal sig : model.getSignals()) {
            if (!sig.isPort()) {
                out.write("  signal " + sig.getName() + ": " + getType(sig.getBits()) + ";\n");
            }
        }

        out.write("begin\n");
        int g = 0;
        for (HDLNode node : model) {
            out.write("  gate" + (g++) + " : " + getVhdlEntityName(node) + "\n");
            writePortMap(node);
        }
        out.write("end " + name + "_arch;\n");
    }

    private void writePortMap(HDLNode node) throws IOException {
        out.write("    port map ( ");
        Separator comma = new Separator(" , ");
        for (Port p : node.getPorts()) {
            comma.check(out);
            if (p.getSignal().isPort())
                out.write(vhdlName(p.getName()) + " => " + vhdlName(p.getSignal().getName()));
            else
                out.write(vhdlName(p.getName()) + " => " + p.getSignal().getName());
        }
        out.write(" );\n");
    }

    private String getVhdlEntityName(HDLNode node) {
        return node.getVisualElement().getElementName();
    }

    private void writeComponent(HDLNode node) throws IOException, ElementNotFoundException, NodeException, PinException, HDLException {
        out.write("\n  component " + getVhdlEntityName(node) + "\n");
        writePort("    ", node.getPorts());
        out.write("  end component;\n");
    }

    private void writePort(String blanks, Ports ports) throws IOException, HDLException {
        out.write(blanks + "port (\n");
        Separator semic = new Separator(";\n");
        for (Port p : ports) {
            semic.check(out);
            out.write(blanks + "  " + vhdlName(p.getName()) + ": " + getDirection(p) + " " + getType(p.getBits()));
        }
        out.write(" );\n");
    }

    private String vhdlName(String name) {
        return "_" + name + "_";
    }

    private String getDirection(Port p) throws HDLException {
        switch (p.getDirection()) {
            case in:
                return "in";
            case out:
                return "out";
            default:
                throw new HDLException(Lang.get("err_vhdlUnknownPortType_N", p.getDirection().toString()));
        }
    }

    private String getType(int bits) {
        if (bits == 1)
            return "std_logic";
        else
            return "std_logic_vector (" + (bits - 1) + " downto 0)";
    }

    @Override
    public String toString() {
        if (stringWriter != null) {
            try {
                out.flush();
            } catch (IOException e) {
                // can not happen
            }
            return stringWriter.toString();
        } else
            return "unknown";
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    private final static class Separator {
        private final String sep;
        private boolean first = true;

        private Separator(String sep) {
            this.sep = sep;
        }

        public void check(BufferedWriter out) throws IOException {
            if (first)
                first = false;
            else
                out.write(sep);
        }
    }
}
