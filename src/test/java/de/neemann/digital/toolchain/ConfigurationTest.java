/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ConfigurationTest extends TestCase {

    public void testStart() throws Exception {
        String xml = "<toolchain name=\"APIO\">\n" +
                "    <commands>\n" +
                "        <command name=\"build\" requires=\"verilog\" filter=\"false\">\n" +
                "            <arg>make</arg>\n" +
                "        </command>\n" +
                "        <command name=\"prog\" requires=\"verilog\" filter=\"true\">\n" +
                "            <arg>make</arg>\n" +
                "            <arg>{?=dir?}/{?=shortname?}.v</arg>\n" +
                "        </command>\n" +
                "    </commands>\n" +
                " </toolchain>\n";


        ToBreakRunner br = new ToBreakRunner(new File(Resources.getRoot(), "dig/hdl/negSimple.dig"));

        final TestIOInterface fileWriter = new TestIOInterface();
        Configuration c = Configuration.load(new ByteArrayInputStream(xml.getBytes()))
                .setFilenameProvider(() -> new File("z/test.dig"))
                .setCircuitProvider(br::getCircuit)
                .setLibraryProvider(br::getLibrary)
                .setIoInterface(fileWriter);
        ArrayList<Command> commands = c.getCommands();
        assertEquals(2, commands.size());

        c.executeCommand(commands.get(0), null, null).join();

        assertEquals(1, fileWriter.files.size());
        assertTrue(fileWriter.files.containsKey("z/test.v"));

        assertEquals(1, fileWriter.commands.size());
        assertEquals("z", fileWriter.commands.get(0).dir.getPath());
        assertEquals("[make]", Arrays.toString(fileWriter.commands.get(0).args));

        fileWriter.clear();
        c.executeCommand(commands.get(1), null, null).join();

        assertEquals(1, fileWriter.files.size());
        assertTrue(fileWriter.files.containsKey("z/test.v"));

        assertEquals(1, fileWriter.commands.size());
        assertEquals("z", fileWriter.commands.get(0).dir.getPath());
        assertEquals("[make, z/test.v]", Arrays.toString(fileWriter.commands.get(0).args));
    }

    public void testFileWriter() throws Exception {
        String xml = "<toolchain name=\"APIO\">\n" +
                "    <commands>\n" +
                "        <command name=\"build\" requires=\"verilog\" filter=\"false\">\n" +
                "            <arg>make</arg>\n" +
                "        </command>\n" +
                "    </commands>\n" +
                "    <files>\n" +
                "        <file name=\"file1\" overwrite=\"true\" filter=\"false\">\n" +
                "            <content>deal with {?=path?}</content>\n" +
                "        </file>\n" +
                "        <file name=\"file2\" overwrite=\"true\" filter=\"true\">\n" +
                "            <content>deal with {?=path?}, Bits: {?=model.ports[0].bits?} ({?=model.ports[0].name?})</content>\n" +
                "        </file>\n" +
                "        <file name=\"{?=shortname?}.z\" overwrite=\"true\" filter=\"false\">\n" +
                "            <content>test</content>\n" +
                "        </file>\n" +
                "    </files>\n" +
                " </toolchain>\n";


        ToBreakRunner br = new ToBreakRunner(new File(Resources.getRoot(), "dig/hdl/negSimple.dig"));

        final TestIOInterface fileWriter = new TestIOInterface();
        Configuration c = Configuration.load(new ByteArrayInputStream(xml.getBytes()))
                .setFilenameProvider(() -> new File("z/test.dig"))
                .setCircuitProvider(br::getCircuit)
                .setLibraryProvider(br::getLibrary)
                .setIoInterface(fileWriter);
        ArrayList<Command> commands = c.getCommands();
        assertEquals(1, commands.size());

        c.executeCommand(commands.get(0), null, null).join();

        assertEquals(4, fileWriter.files.size());
        assertEquals("deal with {?=path?}", fileWriter.files.get("z/file1").toString());
        assertEquals("deal with z/test.dig, Bits: 1 (A)", fileWriter.files.get("z/file2").toString().replace('\\', '/'));
        assertEquals("test", fileWriter.files.get("z/test.z").toString());
    }


    static class TestIOInterface implements Configuration.IOInterface {
        private HashMap<String, ByteArrayOutputStream> files = new HashMap<>();
        private ArrayList<StartedCommand> commands = new ArrayList<>();

        @Override
        public OutputStream getOutputStream(File filename) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            files.put(filename.getPath().replace('\\', '/'), baos);
            return baos;
        }

        @Override
        public void startProcess(Command command, File dir, String[] args) {
            commands.add(new StartedCommand(dir, args));
        }

        @Override
        public void showError(Command command, Exception e) {
            fail(e.getMessage());
        }

        void clear() {
            files.clear();
            commands.clear();
        }

        public HashMap<String, ByteArrayOutputStream> getFiles() {
            return files;
        }
    }

    private static class StartedCommand {
        private final File dir;
        private final String[] args;

        private StartedCommand(File dir, String[] args) {
            this.dir = dir;
            this.args = args;
        }
    }
}