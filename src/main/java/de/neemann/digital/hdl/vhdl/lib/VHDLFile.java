package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLNode;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.*;
import java.util.ArrayList;

/**
 * Reads a file with the vhdl code to create the entity
 */
public class VHDLFile implements VHDLEntity {

    private final String name;
    private final ArrayList<String> vhdl;
    private final boolean hasData;
    private final Interval port;
    private final Interval arch;
    private boolean written = false;
    private boolean writtenBus = false;

    /**
     * Creates a new instance
     *
     * @param name the filename
     * @throws IOException IOException
     */
    public VHDLFile(String name) throws IOException {
        this.name = name;
        vhdl = readFile(name);
        hasData = hasdata();
        port = extract("entity " + name + " is", "end " + name + ";");
        arch = extract("architecture " + name + "_arch of " + name + " is", "end " + name + "_arch;");
    }

    private boolean hasdata() {
        for (String s : vhdl)
            if (s.contains("{{data}}"))
                return true;
        return false;
    }

    private Interval extract(String start, String end) throws IOException {
        return new Interval(indexOf(start), indexOf(end));
    }

    private int indexOf(String text) throws IOException {
        for (int i = 0; i < vhdl.size(); i++)
            if (vhdl.get(i).equalsIgnoreCase(text))
                return i;
        throw new IOException("string " + text + " not found!");
    }

    private ArrayList<String> readFile(String name) throws IOException {
        ArrayList<String> vhdl = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("vhdl/" + name + ".vhdl")))) {
            String line;
            while ((line = in.readLine()) != null)
                vhdl.add(line);
        }
        return vhdl;
    }

    @Override
    public void writeHeader(CodePrinter out, HDLNode node) throws IOException {
        for (int i = 0; i < port.start - 1; i++)
            out.println(vhdl.get(i));
    }

    @Override
    public String getName(HDLNode node) {
        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                return name + "_BUS";
            else
                return name;
        } else
            return name;
    }

    @Override
    public boolean needsOutput(HDLNode node) {
        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                return !writtenBus;
            else
                return !written;
        } else
            return !written;
    }

    @Override
    public void writeDeclaration(CodePrinter out, HDLNode node) throws IOException, HDLException {
        if (hasData && node.get(Keys.BITS) > 1)
            out.println("generic ( bitCount : integer );");
        for (int i = port.start + 1; i < port.end; i++)
            out.println(transform(vhdl.get(i), node));
    }

    private String transform(String s, HDLNode node) throws HDLException {
        String type;
        if (hasData && node.get(Keys.BITS) > 1) {
            type = "std_logic_vector((bitCount-1) downto 0)";
        } else
            type = "std_logic";
        return s.replace("{{data}}", type);
    }


    @Override
    public void writeArchitecture(CodePrinter out, HDLNode node) throws IOException, HDLException {
        for (int i = arch.start + 1; i < arch.end - 1; i++)
            out.println(transform(vhdl.get(i), node));

        if (hasData) {
            if (node.get(Keys.BITS) > 1)
                writtenBus = true;
            else
                written = true;
        } else
            written = true;
    }

    @Override
    public void writeGenericMap(CodePrinter out, HDLNode node) throws IOException, HDLException {
        if (hasData && node.get(Keys.BITS) > 1)
            out.print("generic map ( bitCount => ").print(node.get(Keys.BITS)).println(")");
    }

    @Override
    public boolean createsSignals() {
        return true;
    }

    private static final class Interval {
        private final int start;
        private final int end;

        private Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
