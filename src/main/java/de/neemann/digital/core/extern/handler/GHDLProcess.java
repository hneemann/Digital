/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.extern.PortDefinition;

import java.io.File;
import java.io.IOException;

/**
 * Can use GHDL as VHDL simulator.
 */
public class GHDLProcess extends VHDLProcess {

    /**
     * Creates a new instance
     *
     * @param label      the label
     * @param code       the code
     * @param inputs     th inputs to use
     * @param outputs    the outputs to use
     * @throws IOException IOException
     */
    public GHDLProcess(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        super(label, code, inputs, outputs);

        String ghdl = "ghdl";

        File file = getVHDLFile();
        ProcessStarter.start(file.getParentFile(), ghdl, "-a", "--ieee=synopsys", file.getName());
        ProcessStarter.start(file.getParentFile(), ghdl, "-e", "--ieee=synopsys", "stdIOInterface");
        ProcessBuilder pb = new ProcessBuilder(ghdl, "-r", "--ieee=synopsys", "stdIOInterface").redirectErrorStream(true).directory(file.getParentFile());
        setProcess(pb.start());
    }
}
