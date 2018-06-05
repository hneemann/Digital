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
        BoardInformation bi = getBoardInfo();

        w.write(String.format(iseProjectTplt, "../" + srcFile.getName(),
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
