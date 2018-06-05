/*
 * Copyright (c) 2018 Ivan Deras. Adapted from the Vivado exporter.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.analyse.SplitPinString;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.*;

/**
 * Creates the needed ISE files.
 * Up to now only the constraints files containing the pin assignments and project file is created
 */
public abstract class ISE implements BoardInterface {
    private static final String ISE_PROJECT_TPLT =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n"
            + "<project xmlns=\"http://www.xilinx.com/XMLSchema\" xmlns:xil_pn=\"http://www.xilinx.com/XMLSchema\">\n"
            + "  <header>\n"
            + "  </header>\n"
            + "  <version xil_pn:ise_version=\"14.7\" xil_pn:schema_version=\"2\"/>\n"
            + "  <files>\n"
            + "    <file xil_pn:name=\"%1$s\" xil_pn:type=\"FILE_VERILOG\">\n"
            + "      <association xil_pn:name=\"BehavioralSimulation\" xil_pn:seqID=\"2\"/>\n"
            + "      <association xil_pn:name=\"Implementation\" xil_pn:seqID=\"2\"/>\n"
            + "    </file>\n"
            + "    <file xil_pn:name=\"%2$s\" xil_pn:type=\"FILE_UCF\">\n"
            + "      <association xil_pn:name=\"Implementation\" xil_pn:seqID=\"0\"/>\n"
            + "    </file>\n"
            + "  </files>\n"
            + "  <autoManagedFiles>\n"
            + "  </autoManagedFiles>\n"
            + "  <properties>\n"
            + "    <property xil_pn:name=\"Create Binary Configuration File\" xil_pn:value=\"true\" xil_pn:valueState=\"non-default\"/>\n"
            + "    <property xil_pn:name=\"Create Bit File\" xil_pn:value=\"true\" xil_pn:valueState=\"default\"/>\n"
            + "    <property xil_pn:name=\"Device Family\" xil_pn:value=\"%3$s\" xil_pn:valueState=\"non-default\"/>\n"
            + "    <property xil_pn:name=\"Device\" xil_pn:value=\"%4$s\" xil_pn:valueState=\"non-default\"/>\n"
            + "    <property xil_pn:name=\"Package\" xil_pn:value=\"%5$s\" xil_pn:valueState=\"non-default\"/>\n"
            + "    <property xil_pn:name=\"Implementation Top File\" xil_pn:value=\"%1$s\" xil_pn:valueState=\"non-default\"/>\n"
            + "    <property xil_pn:name=\"Working Directory\" xil_pn:value=\".\" xil_pn:valueState=\"non-default\"/>\n"
            + "  </properties>\n"
            + "  <bindings/>\n"
            + "  <libraries/>\n"
            + "</project>\n";

    @Override
    public void writeFiles(File path, HDLModel model) throws IOException {
        String projectName = path.getName();
        if (projectName.endsWith(".v"))
            projectName = projectName.substring(0, projectName.length() - 2);
        else if (projectName.endsWith(".vhdl"))
            projectName = projectName.substring(0, projectName.length() - 5);
        File constraints = new File(path.getParentFile(), projectName.replace('.', '_') + "_constraints.ucf");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(constraints))) {
            writeConstraints(out, model);
        }
        createISEProject(path.getParentFile(), projectName, path, constraints);
    }

    private void writeConstraints(CodePrinter out, HDLModel model) throws IOException {
        for (HDLPort p : model.getMain().getPorts()) {
            if (p.getBits() == 1) {
                writePin(out, p.getName(), p.getPinNumber());
            } else {
                SplitPinString pins = SplitPinString.create(p.getPinNumber());
                for (int i = 0; i < p.getBits(); i++) {
                    writePin(out, p.getName() + "[" + i + "]", pins.getPin(i));
                    out.println();
                }
            }

            out.println();
        }
    }

    /**
     * Write the pin information to a Xilinx UCF (User Constraints File)
     *
     * @param out          the code printer
     * @param name         the signal name
     * @param pinNumber    the pin name
     * @throws IOException IOException
     */
    abstract void writePin(CodePrinter out, String name, String pinNumber) throws IOException;

    /**
     * Returns the FPGA board information (Family, Code and Package in that order)
     *
     * @return The board information
     */
    abstract BoardInformation getBoardInfo();

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

    private void writeISEProject(BufferedWriter w, File project, File srcFile, File constraints) throws IOException {
        BoardInformation bi = getBoardInfo();

        w.write(String.format(ISE_PROJECT_TPLT, "../" + srcFile.getName(),
                "../" + constraints.getName(), bi.getFamily(), bi.getCode(), bi.getPkg()));
    }

    static class BoardInformation {
        private final String family;
        private final String code;
        private final String pkg;

        BoardInformation(String family, String code, String pkg) {
            this.family = family;
            this.code = code;
            this.pkg = pkg;
        }

        public String getFamily() {
            return family;
        }

        public String getCode() {
            return code;
        }

        public String getPkg() {
            return pkg;
        }
    }
}
