/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

import de.neemann.digital.analyse.SplitPinString;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;

import java.io.*;

/**
 * Creates the needed vivado files.
 * Up to now only the constraints file containing the pin assignments is created
 */
public class Vivado implements BoardInterface {

    private final String pinIoType;
    private final String clockPin;
    private final int periodns;
    private final HDLClockIntegrator clockIntegrator;
    private final String device;

    /**
     * Creates a new instance
     *
     * @param pinIoType       the pin output type
     * @param clockPin        the pin the clock is connected to
     * @param periodns        the clock period in nano seconds
     * @param clockIntegrator the clock integrator to use
     * @param device          the xilinx device code
     */
    public Vivado(String pinIoType, String clockPin, int periodns, HDLClockIntegrator clockIntegrator, String device) {
        this.pinIoType = pinIoType;
        this.clockPin = clockPin;
        this.periodns = periodns;
        this.clockIntegrator = clockIntegrator;
        this.device = device;
    }

    @Override
    public void writeFiles(File path, HDLModel model) throws IOException {
        String projectName = path.getName();
        if (projectName.endsWith(".vhdl"))
            projectName = projectName.substring(0, projectName.length() - 5);
        File constraints = new File(path.getParentFile(), projectName.replace('.', '_') + "_constraints.xdc");
        try (CodePrinter out = new CodePrinter(new FileOutputStream(constraints))) {
            writeConstraints(out, model);
        }
        createVivadoProject(path.getParentFile(), projectName, path, constraints);
    }

    private void writeConstraints(CodePrinter out, HDLModel model) throws IOException {
        for (HDLPort p : model.getMain().getPorts()) {
            if (p.getBits() == 1) {
                writePin(out, p.getName(), p.getPinNumber());
                if (p.getPinNumber().equals(clockPin))
                    out
                            .print("create_clock -add -name sys_clk_pin -period ")
                            .print(periodns)
                            .print(" -waveform {0 5} [get_ports ")
                            .print(p.getName())
                            .println("]");
            } else {
                SplitPinString pins = SplitPinString.create(p.getPinNumber());
                for (int i = 0; i < p.getBits(); i++)
                    writePin(out, p.getName() + "[" + i + "]", pins.getPin(i));
            }


            out.println();
        }

        out.println("set_property CFGBVS VCCO  [current_design]");
        out.println("set_property CONFIG_VOLTAGE 3.3 [current_design]");
    }

    private void writePin(CodePrinter out, String name, String pinNumber) throws IOException {
        if (pinNumber == null || pinNumber.length() == 0)
            throw new IOException(Lang.get("err_vhdlPin_N_hasNoNumber", name));

        out.print("set_property PACKAGE_PIN ").print(pinNumber).print(" [get_ports ").print(name).println("]");
        out.print("set_property IOSTANDARD ").print(pinIoType).print(" [get_ports ").print(name).println("]");
    }

    @Override
    public HDLClockIntegrator getClockIntegrator() {
        return clockIntegrator;
    }

    private void createVivadoProject(File path, String projectName, File vhdl, File constraints) throws IOException {
        String projectDir = projectName + "_vivado";
        File projectPath = new File(path, projectDir);
        // don't overwrite existing projects!
        if (!projectPath.exists()) {
            if (projectPath.mkdirs()) {
                File projectFile = new File(projectPath, projectName + ".xpr");
                try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(projectFile), "utf-8"))) {
                    writeVivadoProject(w, projectFile, vhdl, constraints);
                }
            }
        }
    }

    private void writeVivadoProject(BufferedWriter w, File project, File vhdl, File constraints) throws IOException {
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<!-- Created by Digital -->\n"
                + "\n"
                + "<Project Version=\"7\" Minor=\"20\" Path=\"" + project.getPath() + "\">\n"
                + "  <DefaultLaunch Dir=\"$PRUNDIR\"/>\n"
                + "  <Configuration>\n"
                + "    <Option Name=\"Part\" Val=\"" + device + "\"/>\n"
                + "  </Configuration>\n"
                + "  <FileSets Version=\"1\" Minor=\"31\">\n"
                + "    <FileSet Name=\"sources_1\" Type=\"DesignSrcs\" RelSrcDir=\"$PSRCDIR/sources_1\">\n"
                + "      <Filter Type=\"Srcs\"/>\n"
                + "      <File Path=\"$PPRDIR/../" + vhdl.getName() + "\">\n"
                + "        <FileInfo>\n"
                + "          <Attr Name=\"UsedIn\" Val=\"synthesis\"/>\n"
                + "          <Attr Name=\"UsedIn\" Val=\"simulation\"/>\n"
                + "        </FileInfo>\n"
                + "      </File>\n"
                + "      <Config>\n"
                + "        <Option Name=\"DesignMode\" Val=\"RTL\"/>\n"
                + "        <Option Name=\"TopModule\" Val=\"main\"/>\n"
                + "        <Option Name=\"TopAutoSet\" Val=\"TRUE\"/>\n"
                + "      </Config>\n"
                + "    </FileSet>\n"
                + "    <FileSet Name=\"constrs_1\" Type=\"Constrs\" RelSrcDir=\"$PSRCDIR/constrs_1\">\n"
                + "      <Filter Type=\"Constrs\"/>\n"
                + "      <File Path=\"$PPRDIR/../" + constraints.getName() + "\">\n"
                + "        <FileInfo>\n"
                + "          <Attr Name=\"UsedIn\" Val=\"synthesis\"/>\n"
                + "          <Attr Name=\"UsedIn\" Val=\"implementation\"/>\n"
                + "        </FileInfo>\n"
                + "      </File>\n"
                + "      <Config>\n"
                + "        <Option Name=\"ConstrsType\" Val=\"XDC\"/>\n"
                + "      </Config>\n"
                + "    </FileSet>\n"
                + "  </FileSets>\n"
                + "</Project>");
    }

}
