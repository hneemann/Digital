package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

import java.io.*;

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
     * @throws IOException IOException
     */
    public void export(Circuit circuit) throws IOException {
        try {
            export(circuit, "main");
            close();
        } catch (PinException | VHDLException | ElementNotFoundException | NodeException e) {
            throw new IOException(Lang.get("err_exporting_vhdl"), e);
        }
    }

    private void export(Circuit circuit, String name) throws IOException, PinException, VHDLException, ElementNotFoundException, NodeException {
        out.write("entity " + name + " is\n");
        writePort(circuit);
        out.write("end " + name + ";\n");

        out.write("\narchitecture " + name + "_arch of " + name + " is\n");

        for (VisualElement ve : circuit.getElements()) {
            if (isComponent(ve))
                writeComponent(ve);
        }

        out.write("begin\n");
        out.write("end " + name + "_arch;\n");
    }

    private boolean isComponent(VisualElement ve) {
        return !(ve.equalsDescription(In.DESCRIPTION)
                || ve.equalsDescription(Out.DESCRIPTION)
                || ve.equalsDescription(Clock.DESCRIPTION));
    }

    private void writeComponent(VisualElement ve) throws IOException, ElementNotFoundException, NodeException, PinException {
        out.write("  component " + ve.getElementName() + "\n");
        ElementTypeDescription td = library.getElementType(ve.getElementName());
        PinDescriptions inputs = td.getInputDescription(ve.getElementAttributes());
        PinDescriptions outputs = td.getOutputDescriptions(ve.getElementAttributes());
        out.write("  port (\n");
        out.write("\n  );\n");
        out.write("  end component;\n");
    }

    VHDLExporter writePort(Circuit circuit) throws IOException {
        out.write("  port (\n");
        Semicolon semic = new Semicolon();
        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION)) {
                semic.check(out);
                ElementAttributes attr = ve.getElementAttributes();
                out.write("    " + attr.getCleanLabel() + ": in " + getType(attr.getBits()));
            }
            if (ve.equalsDescription(Clock.DESCRIPTION)) {
                ElementAttributes attr = ve.getElementAttributes();
                semic.check(out);
                out.write("    " + attr.getCleanLabel() + ": in " + getType(1));
            }
        }
        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(Out.DESCRIPTION)) {
                semic.check(out);
                ElementAttributes attr = ve.getElementAttributes();
                out.write("    " + attr.getCleanLabel() + ": out " + getType(attr.getBits()));
            }
        }
        out.write("\n  );\n");
        return this;
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

    private static class Semicolon {
        private boolean first = true;

        public void check(BufferedWriter out) throws IOException {
            if (first)
                first = false;
            else
                out.write(";\n");
        }
    }
}
