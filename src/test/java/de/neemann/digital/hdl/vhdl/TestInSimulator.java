package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Test vhdl files in ghdl simulator
 */
public class TestInSimulator extends TestCase {

    private int testBenches;

    public void testInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/hdl");
        int tested = new FileScanner(this::check).scan(examples);
        // if tested is negative ghdl was not found and tests are skipped!
        if (tested >= 0) {
            assertEquals(25, tested);
            assertEquals(2, testBenches);
        }
    }

    private void check(File file) throws PinException, NodeException, ElementNotFoundException, IOException, FileScanner.SkipAllException, HDLException {
        ToBreakRunner br = new ToBreakRunner(file);
        File dir = Files.createTempDirectory("digital_vhdl_test_").toFile();
        File vhdlFile = new File(dir, file.getName().replace('.', '_') + ".vhdl");
        CodePrinter out = new CodePrinter(vhdlFile);
        try (VHDLExporter vhdl = new VHDLExporter(br.getLibrary(), out)) {
            vhdl.export(br.getCircuit());
            VHDLTestBenchCreator tb = vhdl.getTestBenches();
            out.close();
            runGHDL(vhdlFile, tb.getTestFileWritten());
        }
        for (File f : dir.listFiles())
            f.delete();
        dir.delete();
    }

    private void runGHDL(File vhdlFile, ArrayList<File> testFileWritten) throws IOException, FileScanner.SkipAllException, HDLException {
        checkWarn(vhdlFile, startProcess(vhdlFile.getParentFile(), "ghdl", "-a", "--ieee=synopsys", vhdlFile.getName()));
        checkWarn(vhdlFile, startProcess(vhdlFile.getParentFile(), "ghdl", "-e", "--ieee=synopsys", "main"));
        for (File testbench : testFileWritten) {
            String name = testbench.getName();
            checkWarn(testbench, startProcess(vhdlFile.getParentFile(), "ghdl", "-a", "--ieee=synopsys", name));
            String module = name.substring(0, name.length() - 5);
            checkWarn(testbench, startProcess(vhdlFile.getParentFile(), "ghdl", "-e", "--ieee=synopsys", module));
            String result = startProcess(vhdlFile.getParentFile(), "ghdl", "-r", "--ieee=synopsys", module, "--vcd=" + module + ".vcd");
            if (result.contains("(assertion error)"))
                throw new HDLException("test bench " + name + " faild:\n" + result);
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
        //System.out.println("start " + Arrays.toString(args));
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true).directory(dir);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new FileScanner.SkipAllException("ghdl (https://github.com/tgingold/ghdl) is not installed!");
        }
        ReaderThread rt = new ReaderThread(p.getInputStream());
        rt.start();
        try {
            int exitValue = p.waitFor();
            rt.join();

            String output = rt.toString();

            if (exitValue != 0)
                throw new IOException("exit value not null: " + exitValue + "\n" + output);

            return output;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static final class ReaderThread extends Thread {
        private final ByteArrayOutputStream baos;
        private final InputStream in;

        private ReaderThread(InputStream in) {
            this.in = in;
            baos = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            try {
                try {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) > 0)
                        baos.write(buffer, 0, len);
                } finally {
                    in.close();
                }
            } catch (IOException e) {

            }
        }

        @Override
        public String toString() {
            return baos.toString();
        }
    }

}
