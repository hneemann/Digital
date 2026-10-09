/*
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

package de.neemann.digital.cli;

import java.io.File;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.hdl.vhdl2.VHDLGenerator;
import de.neemann.digital.lang.Lang;

/**
 * Command to convert dig to vhdl or verilog
 */
public class HDLExport extends BasicCommand {
    private final Argument<String> digFile;
    private final Argument<String> hdlFile;
    private final Argument<String> language;

    /**
     * Creates the run command
     */
    public HDLExport() {
        super("export");
        digFile = addArgument(new Argument<>("dig", "", false));
        hdlFile = addArgument(new Argument<>("hdl", "", true));
        language = addArgument(new Argument<>("language", "", false));
    }

    @Override
    public void execute() throws CLIException {

        String digFilePath = digFile.get();
        String lang = language.get().toLowerCase(); // normalize string input

        // Validate the language
        if (!lang.equals("verilog") && !lang.equals("vhdl")) {
            throw new CLIException(Lang.get("cli_errorLanguageInvalid"), null);
        }

        // Validate the dig file
        File digFileObj = new File(digFile.get());
        if (!digFileObj.exists()) {
            throw new CLIException(Lang.get("cli_errorDigFileNotFound"), null);
        }

        // Determine output file based on arguments, default is dig name/path converted
        // to HDL
        String digFileName = digFileObj.getName();
        String digDir = digFileObj.getParent();
        File hdlFileObj;
        if (hdlFile.isSet()) {
            hdlFileObj = new File(hdlFile.get());
        } else {
            String baseName = digFileName.replaceAll("\\.dig$", "");
            String defaultHDLFileName = lang.equals("verilog") ? baseName + ".v" : baseName + ".vhd";
            hdlFileObj = new File(digDir, defaultHDLFileName);
        }

        try {
            CircuitLoader circuitLoader = new CircuitLoader(digFilePath, false);

            if (lang.equals("verilog")) {
                try (
                        VerilogGenerator verilogGenerator = new VerilogGenerator(
                                circuitLoader.getLibrary(),
                                new CodePrinter(hdlFileObj))) {
                    verilogGenerator.export(circuitLoader.getCircuit());
                }
            } else {
                try (VHDLGenerator vhdlGenerator = new VHDLGenerator(
                        circuitLoader.getLibrary(),
                        new CodePrinter(hdlFileObj))) {
                    vhdlGenerator.export(circuitLoader.getCircuit());
                }
            }

            System.out.println("Export completed successfully to " + hdlFileObj.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace(); // <-- ADD THIS
            throw new CLIException(Lang.get("cli_errorExportFailed"), e);
        }
    }
}
