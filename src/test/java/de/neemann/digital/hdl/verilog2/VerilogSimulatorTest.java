/*
 * Copyright (c) 2018 Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.ProcessStarter;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.TestExamples;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class VerilogSimulatorTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerilogSimulatorTest.class);
    private static String IVERILOG = System.getProperty("iverilog", "");
    private static String IVERILOG_DIR;
    private static String VVP;
    private static final boolean foundIVerilog = findIVerilogDir();
    private int testBenches;

    /*
    public void testDebug() throws Exception {
        File file = new File(Resources.getRoot(), "/dig/test/vhdl/driver1inv.dig");

        ToBreakRunner br = new ToBreakRunner(file);
        System.out.println(new VerilogGenerator(br.getLibrary(), new CodePrinterStr(true)).export(br.getCircuit()));

        checkVerilogExport(file);
    }/**/

    public void testInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test/vhdl");
        try {
            int tested = new FileScanner(this::checkVerilogExport).noOutput().scan(examples);
            assertEquals(70, tested);
            assertEquals(60, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        }
    }

    public void testInSimulator2() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/hdl");
        try {
            int tested = new FileScanner(this::checkVerilogExport).noOutput().scan(examples);
            assertEquals(53, tested);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        }
    }

    public void testInSimulatorInOut() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test/pinControl");
        try {
            int tested = new FileScanner(f -> {
                if (!f.getName().equals("uniTest.dig"))
                    checkVerilogExport(f);
            }).noOutput().scan(examples);
            assertEquals(2, tested);
            assertEquals(1, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        }
    }


    public void testDistributedInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "../../main/dig/hdl");
        try {
            int tested = new FileScanner(this::checkVerilogExport).noOutput().scan(examples);
            assertEquals(2, tested);
            assertEquals(1, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        }
    }

    public void testProcessorInSimulator() throws Exception {
        File file = new File(Resources.getRoot(), "../../main/dig/processor/HDLExample.dig");
        try {
            checkVerilogExport(file);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        } catch (Exception e) {
            System.out.println(ExceptionWithOrigin.getOriginOf(e));
            throw e;
        }
    }

    public void testMultiplierInSimulator() throws Exception {
        File file = new File(Resources.getRoot(), "../../main/dig/combinatorial/Multiply8Bit.dig");
        try {
            checkVerilogExport(file);
        } catch (FileScanner.SkipAllException e) {
            // if iverilog is not installed its also ok
        } catch (Exception e) {
            System.out.println(ExceptionWithOrigin.getOriginOf(e));
            throw e;
        }
    }

    public void testIVERILOGInSimulator() throws Exception {
        if (foundIVerilog) {
            Settings.getInstance().getAttributes().set(Keys.SETTINGS_IVERILOG_PATH, new File(IVERILOG));

            File source = new File(Resources.getRoot(), "dig/external/verilog");

            int tested = new FileScanner(f -> {
                checkVerilogExport(f);
                // check simulation in Digital
                new TestExamples().check(f);
            }).scan(source);
            assertEquals(5, tested);
        }
    }


    private void checkVerilogExport(File file) throws Exception {
        ToBreakRunner br = new ToBreakRunner(file);
        File dir = Files.createTempDirectory("digital_verilog_" + getTime() + "_").toFile();
        try {
            File srcFile = new File(dir, file.getName()
                    .replace('.', '_')
                    .replace('-', '_') + ".v");
            CodePrinter out = new CodePrinter(srcFile);
            try (VerilogGenerator gen = new VerilogGenerator(br.getLibrary(), out)) {
                gen.export(br.getCircuit());
                ArrayList<File> testFiles = gen.getTestBenches();
                out.close();
                runIVerilog(srcFile, testFiles);
            }
            ProcessStarter.removeFolder(dir);
        } finally {
            br.close();
        }
    }

    private void runIVerilog(File sourceFile, ArrayList<File> testFileWritten) throws IOException, FileScanner.SkipAllException, HDLException {
        String ivlModuleDir = IVERILOG_DIR + File.separator + "lib" + File.separator + "ivl";
        for (File testbench : testFileWritten) {
            String name = testbench.getName();
            String module = name.substring(0, name.length() - 2);
            String testOutputName = module + ".out";

            checkWarn(testbench, startProcess(sourceFile.getParentFile(), IVERILOG, "-tvvp", "-o" + testOutputName, sourceFile.getName(), name));

            String result = startProcess(sourceFile.getParentFile(), VVP, "-M", ivlModuleDir, testOutputName);
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
            throw new FileScanner.SkipAllException("iverilog (https://github.com/steveicarus/iverilog) is not installed! Add iverilog binary to the system path or set system property 'iverilog' to iverilog binary");
        }
    }

    private String getTime() {
        DateFormat f = new SimpleDateFormat("yy-MM-dd_HH-mm_ss");
        return f.format(new Date());
    }

    private static boolean findIVerilogDir() {
        Path ivp = null;

        if (!IVERILOG.isEmpty()) {
            Path p = Paths.get(IVERILOG);

            if (Files.isExecutable(p)) {
                ivp = p;
                if (Files.isSymbolicLink(p)) {
                    try {
                        ivp = Files.readSymbolicLink(ivp);
                    } catch (IOException ex) {
                        LOGGER.info("I/O Exception: " + ex.getMessage());
                        return false;
                    }
                }
            }
        }

        if (ivp == null) {
            // Let's try to find iverilog in the system path
            String[] strPaths = System.getenv("PATH").split(File.pathSeparator);

            for (String sp : strPaths) {
                Path p = Paths.get(sp, "iverilog");

                if (Files.isExecutable(p)) {
                    ivp = p;
                    if (Files.isSymbolicLink(p)) {
                        try {
                            ivp = Files.readSymbolicLink(ivp);
                        } catch (IOException ex) {
                            LOGGER.info("I/O Exception: " + ex.getMessage());
                            return false;
                        }
                    }
                    break;
                }
            }
        }

        if (ivp != null) {
            IVERILOG_DIR = ivp.getParent().getParent().toString();
            IVERILOG = ivp.getParent().resolve("iverilog").toString();
            VVP = ivp.getParent().resolve("vvp").toString();

            return true;
        } else {
            return false;
        }
    }
}