/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.extern.Port;
import de.neemann.digital.core.extern.PortDefinition;

import java.io.*;

/**
 * Creates a VHDL process
 */
public class VHDLProcess extends StdIOProcess {
    private final File file;

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
     * Creates a new instance
     *
     * @param label   the name of the vhdl code
     * @param code    the vhdl code
     * @param inputs  the inputs
     * @param outputs the outputs
     * @throws IOException IOException
     */
    public VHDLProcess(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        String t = InstanceHolder.TEMPLATE;
        t = t.replace("%name%", label);
        t = t.replace("%incount%", Integer.toString(inputs.getBits()));
        t = t.replace("%outcount%", Integer.toString(outputs.getBits()));
        t = t.replace("%ports%", createPorts(inputs, outputs));
        t = t.replace("%signals%", createSignals(inputs, outputs));
        t = t.replace("%map%", createMap(inputs, outputs));
        t = t.replace("%inOutMapping%", createInOutMapping(inputs, outputs));

        file = File.createTempFile(label, ".vhdl");
        try (Writer w = new FileWriter(file)) {
            w.write(code);
            w.write("\n\n\n");
            w.write(t);
        }
    }

    /**
     * @return the created vhdl file
     */
    public File getFile() {
        return file;
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
    public void close() throws IOException {
        super.close();
        //if (file != null) file.delete();
    }
}
