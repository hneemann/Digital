/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.ProcessStarter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VHDLGeneratorTest extends TestCase {
    private static final String GHDL = System.getProperty("ghdl", "ghdl");
    private int testBenches;

    public void testSimple() throws Exception {
        File file = new File(Resources.getRoot(), "dig/test/vhdl/reset.dig");

        ToBreakRunner br = new ToBreakRunner(file);
        System.out.println(new VHDLGenerator(br.getLibrary(), new CodePrinterStr(true)).export(br.getCircuit()));

        try {
            checkVHDLExport(file);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        } catch (Exception e) {
            System.out.println(ExceptionWithOrigin.getOriginOf(e));
            throw e;
        }
    }

    public void testInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test/vhdl");
        try {
            int tested = new FileScanner(this::checkVHDLExport).noOutput().scan(examples);
            assertEquals(32, tested);
            assertEquals(tested+2, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        }
    }

    public void testInSimulator2() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/hdl");
        try {
            int tested = new FileScanner(this::checkVHDLExport).noOutput().scan(examples);
            assertEquals(35, tested);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        }
    }




    private void checkVHDLExport(File file) throws PinException, NodeException, ElementNotFoundException, IOException, FileScanner.SkipAllException, HDLException, de.neemann.digital.hdl.model2.HDLException, HGSEvalException {
        ToBreakRunner br = new ToBreakRunner(file);
        File dir = Files.createTempDirectory("digital_vhdl_" + getTime() + "_").toFile();
        try {
            File vhdlFile = new File(dir, file.getName()
                    .replace('.', '_')
                    .replace('-', '_')+ ".vhdl");
            CodePrinter out = new CodePrinter(vhdlFile);
            try (VHDLGenerator vhdl = new VHDLGenerator(br.getLibrary(), out)) {
                vhdl.export(br.getCircuit());
                ArrayList<File> testFiles = vhdl.getTestBenches();
                out.close();
                runGHDL(vhdlFile, testFiles);
            }
            ProcessStarter.removeFolder(dir);
        } finally {
            br.close();
        }
    }

    private void runGHDL(File vhdlFile, ArrayList<File> testFileWritten) throws IOException, FileScanner.SkipAllException, HDLException {
        checkWarn(vhdlFile, startProcess(vhdlFile.getParentFile(), GHDL, "-a", "--ieee=synopsys", vhdlFile.getName()));
        checkWarn(vhdlFile, startProcess(vhdlFile.getParentFile(), GHDL, "-e", "--ieee=synopsys", "main"));
        for (File testbench : testFileWritten) {
            String name = testbench.getName();
            checkWarn(testbench, startProcess(vhdlFile.getParentFile(), GHDL, "-a", "--ieee=synopsys", name));
            String module = name.substring(0, name.length() - 5);
            checkWarn(testbench, startProcess(vhdlFile.getParentFile(), GHDL, "-e", "--ieee=synopsys", module));
            String result = startProcess(vhdlFile.getParentFile(), GHDL, "-r", "--ieee=synopsys", module, "--vcd=" + module + ".vcd");
            if (result.contains("(assertion error)"))
                throw new HDLException("test bench " + name + " failed:\n" + result);
            checkWarn(testbench, result);
            testBenches++;
        }
    }

    private void checkWarn(File file, String result) {
        if (result.contains("warning")) {
            System.out.println(file);
            System.out.println(result);
        }
    }

    private String startProcess(File dir, String... args) throws IOException, FileScanner.SkipAllException {
        try {
            return ProcessStarter.start(dir, args);
        } catch (ProcessStarter.CouldNotStartProcessException e) {
            throw new FileScanner.SkipAllException("ghdl (https://github.com/tgingold/ghdl) is not installed! Add ghdl binary to the system path or set system property 'ghdl' to ghdl binary");
        }
    }

    private String getTime() {
        DateFormat f = new SimpleDateFormat("YY-MM-dd_HH-mm_ss");
        return f.format(new Date());
    }

}