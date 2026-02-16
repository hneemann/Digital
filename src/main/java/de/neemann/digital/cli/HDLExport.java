/*
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

package de.neemann.digital.cli;

import java.io.File;
import java.io.IOException;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.lang.Lang;

/**
 * Command to convert dig to vhdl or verilog
 */
public class HDLExport extends BasicCommand {
    private final Argument<String> digFile;
    private final Argument<String> HDLFile;
    private final Argument<String> langugage;

    /**
     * Creates the run command
     */
    public HDLExport() {
        super("export");
        digFile = addArgument(new Argument<>("dig", "", false));
        HDLFile = addArgument(new Argument<>("hdl", "", true));
        langugage = addArgument(new Argument<>("language", "", false));
    }

    @Override
    public void execute() throws CLIException {

        String digFilePath = digFile.get();
        String lang = langugage.get().toLowerCase();

        // Validate the language
        if (!lang.equals("verilog") && !lang.equals("vhdl")) {
            throw new CLIException(Lang.get("cli_errorRunningCircuit"), e);
        }

        // Determine output file
        File digFileObj = new File(digFilePath);
        String digFileName = digFileObj.getName();
        String digDir = digFileObj.getParent();

        File hdlFileObj;
        if (HDLFile.isSet()) {
            hdlFileObj = new File(HDLFile.get());
        } else {
            String baseName = digFileName;
            if (digFileName.endsWith(".dig")) {
                baseName = digFileName.substring(0, digFileName.length() - 4);
            }

            if (langugage.get() == "verilog") {
                String defaultHDLFileName = baseName + ".v"; // default to Verilog
            } else if (langugage.get() == "vhdl") {
                String defaultHDLFileName = baseName + ".vhd"; // default to Verilog
            }

            hdlFileObj = new File(digDir, defaultHDLFileName);
        }

        try {
            CircuitLoader circuitLoader = new CircuitLoader(digFilePath, false);
            VerilogGenerator verilogGenerator = new VerilogGenerator(circuitLoader.getLibrary(),
                    new CodePrinter(verilogFile));
            verilogGenerator.export(circuitLoader.getCircuit());
            System.out.println("Verilog export completed successfully.");
        } catch (IOException e) {
            throw new CLIException("Failed to export Verilog: " + e.getMessage(), e);
        }
    }
}
