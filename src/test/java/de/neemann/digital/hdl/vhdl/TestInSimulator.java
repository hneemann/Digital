package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Test vhdl files in ghdl simulator
 */
public class TestInSimulator extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestInSimulator.class);
    private static final String GHDL = System.getProperty("ghdl", "ghdl");

    private int testBenches;

    public void testInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/test/vhdl");
        try {
            int tested = new FileScanner(this::check).noOutput().scan(examples);
            assertEquals(27, tested);
            assertEquals(tested, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        }
    }

    public void testInSimulator2() throws Exception {
        File examples = new File(Resources.getRoot(), "/dig/hdl");
        try {
            int tested = new FileScanner(this::check).noOutput().scan(examples);
            assertEquals(28, tested);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        }
    }

    public void testDistributedInSimulator() throws Exception {
        File examples = new File(Resources.getRoot(), "../../main/dig/vhdl");
        try {
            int tested = new FileScanner(this::check).noOutput().scan(examples);
            assertEquals(1, tested);
            assertEquals(1, testBenches);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        }
    }

    public void testProcessorInSimulator() throws Exception {
        File file = new File(Resources.getRoot(), "../../main/dig/processor/VHDLExample.dig");
        try {
            check(file);
        } catch (FileScanner.SkipAllException e) {
            // if ghdl is not installed its also ok
        } catch (Exception e) {
            System.out.println(ExceptionWithOrigin.getOriginOf(e));
            throw e;
        }
    }

    /*
    public void testInSimulatorDebug() throws Exception {
        File file = new File(Resources.getRoot(),"dig/test/vhdl/registerFile.dig");

        ToBreakRunner br = new ToBreakRunner(file);
        System.out.println(new VHDLGenerator(br.getLibrary(), new CodePrinterStr(true)).export(br.getCircuit()));

        check(file);
    } /* */


    private void check(File file) throws PinException, NodeException, ElementNotFoundException, IOException, FileScanner.SkipAllException, HDLException {
        ToBreakRunner br = new ToBreakRunner(file);
        File dir = Files.createTempDirectory("digital_vhdl_" + getTime() + "_").toFile();
        File vhdlFile = new File(dir, file.getName().replace('.', '_') + ".vhdl");
        CodePrinter out = new CodePrinter(vhdlFile);
        try (VHDLGenerator vhdl = new VHDLGenerator(br.getLibrary(), out)) {
            vhdl.omitClockDividers().export(br.getCircuit());
            VHDLTestBenchCreator tb = vhdl.getTestBenches();
            out.close();
            runGHDL(vhdlFile, tb.getTestFileWritten());
        }
        File[] filesInDir = dir.listFiles();
        if (filesInDir != null)
            for (File f : filesInDir)
                if (!f.delete()) LOGGER.warn("file " + f + " could not be deleted!");
        if (!dir.delete()) LOGGER.warn("dir " + dir + " could not be deleted!");
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
        //System.out.println("start " + Arrays.toString(args));
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true).directory(dir);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new FileScanner.SkipAllException("ghdl (https://github.com/tgingold/ghdl) is not installed! Add ghdl binary to the system path or set system property 'ghdl' to ghdl binary");
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

    private String getTime() {
        DateFormat f = new SimpleDateFormat("YY-MM-dd_HH-mm_ss");
        return f.format(new Date());
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
                // do nothing, simply end the thread
            }
        }

        @Override
        public String toString() {
            return baos.toString();
        }
    }

}
