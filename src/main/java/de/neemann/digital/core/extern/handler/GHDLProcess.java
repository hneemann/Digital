/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.PortDefinition;
import de.neemann.digital.gui.Settings;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Can use GHDL as VHDL simulator.
 */
public class GHDLProcess extends VHDLProcess {

    /**
     * Creates a new instance
     *
     * @param label   the label
     * @param code    the code
     * @param inputs  th inputs to use
     * @param outputs the outputs to use
     * @throws IOException IOException
     */
    public GHDLProcess(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        super(label, code, inputs, outputs);
        try {
            String ghdl = getGhdlPath().getPath();

            File file = getVHDLFile();
            ProcessStarter.start(file.getParentFile(), ghdl, "-a", "--ieee=synopsys", file.getName());
            ProcessStarter.start(file.getParentFile(), ghdl, "-e", "--ieee=synopsys", "stdIOInterface");
            ProcessBuilder pb = new ProcessBuilder(ghdl, "-r", "--ieee=synopsys", "stdIOInterface").redirectErrorStream(true).directory(file.getParentFile());
            setProcess(pb.start());
        } catch (IOException e) {
            close();  // remove created files
            throw e;
        }
    }

    private static File getGhdlPath() {
        return Settings.getInstance().get(Keys.SETTINGS_GHDL_PATH);
    }

    /**
     * @return true if ghdl is installed!
     */
    public static boolean isInstalled() {
        try {
            ProcessStarter.start(null, getGhdlPath().getPath(), "--help");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getConsoleOutNoWarn(LinkedList<String> consoleOut) {
        StringBuilder sb = new StringBuilder();
        for (String s : consoleOut) {
            if (!s.contains("(assertion warning)"))
                sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
