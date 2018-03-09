/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Base class of applications which are able to interprete VHDL-Code.
 * The generated vhdl code is able to operate with the {@link de.neemann.digital.core.extern.handler.StdIOInterface}.
 */
public abstract class ApplicationVHDLStdIO implements Application {

    private static class InstanceHolder {
        private static final String TEMPLATE = loadTemplate();

        private static String loadTemplate() {
            try {
                try (InputStream in = ClassLoader.getSystemResourceAsStream("templates/VHDLStdIOTemplate.templ")) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int r;
                    while ((r = in.read(buffer)) > 0)
                        baos.write(buffer, 0, r);

                    return baos.toString();
                }
            } catch (IOException e) {
                return null;
            }
        }
    }

    /**
     * Creates a vhdl file in a temp directory.
     *
     * @param label   the name of the vhdl code
     * @param code    the vhdl code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @return the vhdl file
     * @throws IOException IOException
     */
    public File createVHDLFile(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        File dir = Files.createTempDirectory("digital_vhdl_").toFile();

        File file = new File(dir, label + ".vhdl");
        try (Writer w = new FileWriter(file)) {
            w.write(createVHDL(label, code, inputs, outputs));
        }

        return file;
    }

    /**
     * Creates vhdl code
     *
     * @param label   the name of the vhdl code
     * @param code    the vhdl code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @return the vhdl code
     * @throws IOException IOException
     */
    public String createVHDL(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        String t = InstanceHolder.TEMPLATE;
        if (t == null)
            throw new IOException("vhdl template not found!");

        t = t.replace("{{name}}", label);
        t = t.replace("{{incount}}", Integer.toString(inputs.getBits() - 1));
        t = t.replace("{{outcount}}", Integer.toString(outputs.getBits() - 1));
        t = t.replace("{{ports}}", createPorts(inputs, outputs));
        t = t.replace("{{signals}}", createSignals(inputs, outputs));
        t = t.replace("{{map}}", createMap(inputs, outputs));
        t = t.replace("{{inOutMapping}}", createInOutMapping(inputs, outputs));

        return code + "\n\n\n" + t;
    }

    private String createPorts(PortDefinition inputs, PortDefinition outputs) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Port p : inputs) {
            if (first) first = false;
            else sb.append(";\n");
            sb.append(p.getName()).append(":").append(" in ").append(vhdlType(p.getBits()));
        }
        for (Port p : outputs) {
            if (first) first = false;
            else sb.append(";\n");
            sb.append(p.getName()).append(":").append(" out ").append(vhdlType(p.getBits()));
        }
        return sb.toString();
    }

    private String createSignals(PortDefinition inputs, PortDefinition outputs) {
        StringBuilder sb = new StringBuilder();
        for (Port p : inputs)
            sb.append("signal in_").append(p.getName()).append(" : ").append(vhdlType(p.getBits())).append(";\n");
        for (Port p : outputs)
            sb.append("signal out_").append(p.getName()).append(" : ").append(vhdlType(p.getBits())).append(";\n");
        return sb.toString();
    }

    private String createMap(PortDefinition inputs, PortDefinition outputs) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Port p : inputs) {
            if (first) first = false;
            else sb.append(",\n");
            sb.append(p.getName()).append(" => in_").append(p.getName());
        }
        for (Port p : outputs) {
            if (first) first = false;
            else sb.append(",\n");
            sb.append(p.getName()).append(" => out_").append(p.getName());
        }
        return sb.toString();
    }

    private String createInOutMapping(PortDefinition inputs, PortDefinition outputs) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (Port p : inputs) {
            sb.append("in_").append(p.getName()).append(" <= mainIn(").append(createIndex(pos, p)).append(");\n");
            pos += p.getBits();
        }
        pos = 0;
        for (Port p : outputs) {
            sb.append("mainOut(").append(createIndex(pos, p)).append(")  <= ").append("out_").append(p.getName()).append(";\n");
            pos += p.getBits();
        }
        return sb.toString();
    }

    private String createIndex(int pos, Port p) {
        if (p.getBits() == 1)
            return Integer.toString(pos);
        else
            return Integer.toString(pos + p.getBits() - 1) + " downto " + Integer.toString(pos);
    }

    private String vhdlType(int bits) {
        if (bits == 1)
            return "std_logic";
        else
            return "std_logic_vector (" + (bits - 1) + " downto 0)";
    }

    @Override
    public boolean ensureConsistency(ElementAttributes attributes) {
        String code = attributes.get(Keys.EXTERNAL_CODE);
        StringTokenizer st = new StringTokenizer(code, "(), :;\t\n\r");
        try {
            while (st.hasMoreTokens()) {
                if (st.nextToken().toLowerCase().equals("entity"))
                    break;
            }

            String label = st.nextToken();

            while (st.hasMoreTokens()) {
                String tok = st.nextToken().toLowerCase();
                if (tok.equals("end"))
                    return false;
                else if (tok.equals("port")) {
                    PortDefinition in = new PortDefinition("");
                    PortDefinition out = new PortDefinition("");
                    scanPorts(st, in, out);
                    if (in.size() > 0 && out.size() > 0) {
                        attributes.set(Keys.LABEL, label);
                        attributes.set(Keys.EXTERNAL_INPUTS, in.toString());
                        attributes.set(Keys.EXTERNAL_OUTPUTS, out.toString());
                        return true;
                    } else
                        return false;
                }
            }
            return false;
        } catch (NoSuchElementException | ParseException e) {
            return false;
        }
    }

    private void scanPorts(StringTokenizer st, PortDefinition in, PortDefinition out) throws ParseException {
        ArrayList<String> vars = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            switch (tok.toLowerCase()) {
                case "in":
                    scanPort(st, vars, in);
                    vars.clear();
                    break;
                case "out":
                    scanPort(st, vars, out);
                    vars.clear();
                    break;
                case "end":
                    return;
                default:
                    vars.add(tok);
            }
        }
    }

    private void scanPort(StringTokenizer st, ArrayList<String> vars, PortDefinition port) throws ParseException {
        switch (st.nextToken().toLowerCase()) {
            case "std_logic":
                for (String var : vars)
                    port.addPort(var, 1);
                break;
            case "std_logic_vector":
                int upper = getNumber(st);
                if (!st.nextToken().toLowerCase().equals("downto"))
                    throw new ParseException();
                int lower = getNumber(st);

                if (lower != 0)
                    throw new ParseException();

                for (String var : vars)
                    port.addPort(var, upper + 1);
                break;
            default:
                throw new ParseException();
        }
    }

    private int getNumber(StringTokenizer st) throws ParseException {
        try {
            return Integer.parseInt(st.nextToken());
        } catch (NumberFormatException e) {
            throw new ParseException();
        }
    }

    private static final class ParseException extends Exception {
    }
}
