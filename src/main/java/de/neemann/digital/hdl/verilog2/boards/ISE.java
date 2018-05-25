/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2.boards;

import de.neemann.digital.hdl.vhdl2.boards.*;
import de.neemann.digital.analyse.SplitPinString;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.*;

/**
 * Creates the needed vivado files.
 * Up to now only the constraints file containing the pin assignments is created
 */
public class ISE implements BoardInterface {
    private final int periodns;
    private final HDLClockIntegrator clockIntegrator;
    private final String devCode;
    private final String devPkg;
    private final UCFPinWriter ucfWriter;

    /**
     * Creates a new instance
     *
     * @param ucfWriter       the UCF pin writer
     * @param periodns        the clock period in nano seconds
     * @param clockIntegrator the clock integrator to use
     * @param devCode         the xilinx device code
     * @param devPkg          the device package
     */
    public ISE(UCFPinWriter ucfWriter, int periodns, HDLClockIntegrator clockIntegrator, String devCode, String devPkg) {
        this.ucfWriter = ucfWriter;
        this.periodns = periodns;
        this.clockIntegrator = clockIntegrator;
        this.devCode = devCode;
        this.devPkg = devPkg;
    }

    @Override
    public void writeFiles(File path, HDLModel model) throws IOException {
        String projectName = path.getName();
        if (projectName.endsWith(".v"))
            projectName = projectName.substring(0, projectName.length() - 2);
        File constraints = new File(path.getParentFile(), projectName.replace('.', '_') + "_constraints.ucf");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(constraints))) {
            writeConstraints(out, model);
        }
        createISEProject(path.getParentFile(), projectName, path, constraints);
    }

    private void writeConstraints(CodePrinter out, HDLModel model) throws IOException {
        for (HDLPort p : model.getMain().getPorts()) {
            if (p.getBits() == 1) {
                ucfWriter.writePin(out, p.getName(), p.getPinNumber());
            } else {
                SplitPinString pins = SplitPinString.create(p.getPinNumber());
                for (int i = 0; i < p.getBits(); i++) {
                    ucfWriter.writePin(out, p.getName() + "[" + i + "]", pins.getPin(i));
                    out.println();
                }
            }

            out.println();
        }
    }

    @Override
    public HDLClockIntegrator getClockIntegrator() {
        return clockIntegrator;
    }

    private void createISEProject(File path, String projectName, File srcFile, File constraints) throws IOException {
        String projectDir = projectName + "_ise";
        File projectPath = new File(path, projectDir);
        // don't overwrite existing projects!
        if (!projectPath.exists()) {
            if (projectPath.mkdirs()) {
                File projectFile = new File(projectPath, projectName + ".xise");
                try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(projectFile), "utf-8"))) {
                    writeISEProject(w, projectFile, srcFile, constraints);
                }
            }
        }
    }

    private String loadISEProjectTemplate() throws IOException {
        String fileName = "boards/ISEProjectTplt.xml";
        InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
        if (in == null) {
            throw new IOException("file not present: " + fileName);
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String str;

        while ((str = r.readLine()) != null) {
                sb.append(str).append("\n");
        }

        return sb.toString();
    }

    private void writeISEProject(BufferedWriter w, File project, File srcFile, File constraints) throws IOException {
        String iseProjectTplt = loadISEProjectTemplate();

        w.write(String.format(iseProjectTplt, "../" + srcFile.getName(),
                "../" + constraints.getName(), "Spartan6", devCode, devPkg));
    }
}
