/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.ide;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.extern.ProcessStarter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.hdl.vhdl2.VHDLGenerator;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;

/**
 * Used to create the IDE integration
 */
public final class Configuration {

    /**
     * Loads a configuration
     *
     * @param file the file to load
     * @return the configuration
     * @throws IOException IOException
     */
    public static Configuration load(File file) throws IOException {
        return load(new FileInputStream(file));
    }

    /**
     * Loads a configuration
     *
     * @param in the file to load
     * @return the configuration
     * @throws IOException IOException
     */
    public static Configuration load(InputStream in) throws IOException {
        try {
            XStream xStream = getxStream();
            return (Configuration) xStream.fromXML(in);
        } catch (RuntimeException e) {
            throw new IOException("error reading XML", e);
        }
    }

    private static XStream getxStream() {
        final XStream xStream = new XStream(new StaxDriver());
        xStream.alias("ide", Configuration.class);
        xStream.aliasAttribute(Configuration.class, "name", "name");
        xStream.alias("command", Command.class);
        xStream.aliasAttribute(Command.class, "name", "name");
        xStream.aliasAttribute(Command.class, "requires", "requires");
        xStream.aliasAttribute(Command.class, "filter", "filter");
        xStream.addImplicitCollection(Command.class, "args", "arg", String.class);
        xStream.alias("file", FileToCreate.class);
        xStream.aliasAttribute(FileToCreate.class, "name", "name");
        xStream.aliasAttribute(FileToCreate.class, "overwrite", "overwrite");
        xStream.aliasAttribute(FileToCreate.class, "filter", "filter");
        return xStream;
    }

    private String name;
    private ArrayList<Command> commands;
    private ArrayList<FileToCreate> files;
    private transient FilenameProvider filenameProvider;
    private transient CircuitProvider circuitProvider;
    private transient LibraryProvider libraryProvider;
    private transient IOInterface ioInterface;


    private Configuration() {
        files = new ArrayList<>();
        commands = new ArrayList<>();
    }

    /**
     * Sets the file name provider
     *
     * @param filenameProvider the file name provider
     * @return this for chained calls
     */
    public Configuration setFilenameProvider(FilenameProvider filenameProvider) {
        this.filenameProvider = filenameProvider;
        return this;
    }

    /**
     * Sets the circuit provider
     *
     * @param circuitProvider the circuit provider
     * @return this for chained calls
     */
    public Configuration setCircuitProvider(CircuitProvider circuitProvider) {
        this.circuitProvider = circuitProvider;
        return this;
    }

    /**
     * Sets the library provider
     *
     * @param libraryProvider the library provider
     * @return this for chained calls
     */
    public Configuration setLibraryProvider(LibraryProvider libraryProvider) {
        this.libraryProvider = libraryProvider;
        return this;
    }

    Configuration setIoInterface(IOInterface ioInterface) {
        this.ioInterface = ioInterface;
        return this;
    }

    /**
     * Creates a menu used to start the commands.
     *
     * @return the menu
     */
    public JMenu createMenu() {
        JMenu menu = new JMenu(name);
        for (Command c : commands)
            menu.add(new JMenuItem(new ExecuteAction(c)));
        return menu;
    }

    private void checkFilesToCreate(File fileToExecute, HDLModel hdlModel) throws HGSEvalException, IOException, ParserException {
        Context context = createContext(fileToExecute, hdlModel);

        if (files != null)
            for (FileToCreate f : files) {
                context.clearOutput();
                Parser p = new Parser(f.getName());
                p.parse().execute(context);
                File filename = new File(fileToExecute.getParent(), context.toString());

                if (f.isOverwrite() || !filename.exists()) {
                    String content = f.getContent();
                    if (f.isFilter()) {
                        context.clearOutput();
                        p = new Parser(content);
                        p.parse().execute(context);
                        content = context.toString();
                    }

                    try (OutputStream out = getIoInterface().getOutputStream(filename)) {
                        out.write(content.getBytes());
                    }
                }
            }
    }

    private Context createContext(File fileToExecute, HDLModel hdlModel) throws HGSEvalException {
        final Context context = new Context()
                .declareVar("path", fileToExecute.getPath())
                .declareVar("dir", fileToExecute.getParentFile())
                .declareVar("name", fileToExecute.getName())
                .declareVar("shortname", createShortname(fileToExecute.getName()));
        if (hdlModel != null)
            context.declareVar("hdl", new ModelAccess(hdlModel.getMain()));
        return context;
    }

    private IOInterface getIoInterface() {
        if (ioInterface == null)
            ioInterface = new DefaultIOInterface();
        return ioInterface;
    }

    private String createShortname(String name) {
        int p = name.lastIndexOf('.');
        if (p >= 0)
            return name.substring(0, p);
        return name;
    }

    private HDLModel writeHDL(String hdl, File digFile) throws IOException {
        switch (hdl) {
            case "verilog":
                File verilogFile = SaveAsHelper.checkSuffix(digFile, "v");
                final CodePrinter verilogPrinter = new CodePrinter(getIoInterface().getOutputStream(verilogFile));
                try (VerilogGenerator vlog = new VerilogGenerator(libraryProvider.getCurrentLibrary(), verilogPrinter)) {
                    vlog.export(circuitProvider.getCurrentCircuit());
                    return vlog.getModel();
                }
            case "vhdl":
                File vhdlFile = SaveAsHelper.checkSuffix(digFile, "vhdl");
                final CodePrinter vhdlPrinter = new CodePrinter(getIoInterface().getOutputStream(vhdlFile));
                try (VHDLGenerator vlog = new VHDLGenerator(libraryProvider.getCurrentLibrary(), vhdlPrinter)) {
                    vlog.export(circuitProvider.getCurrentCircuit());
                    return vlog.getModel();
                }
            default:
                throw new IOException(Lang.get("err_hdlNotKnown_N", hdl));
        }
    }

    /**
     * Executes the given command
     *
     * @param command the command
     */
    public void executeCommand(Command command) {
        File digFile = filenameProvider.getCurrentFilename();
        if (digFile != null) {
            try {

                HDLModel hdlModel = null;
                if (command.needsHDL())
                    hdlModel = writeHDL(command.getHDL(), digFile);

                checkFilesToCreate(digFile, hdlModel);

                String[] args = command.getArgs();
                if (command.isFilter()) {
                    final int argCount = command.getArgs().length;
                    Context context = createContext(digFile, hdlModel);
                    for (int i = 0; i < argCount; i++) {
                        context.clearOutput();
                        new Parser(args[i]).parse().execute(context);
                        args[i] = context.toString();
                    }
                }
                if (args != null)
                    getIoInterface().startProcess(digFile.getParentFile(), args);
            } catch (Exception e) {
                getIoInterface().showError(command, e);
            }
        }
    }

    private final class ExecuteAction extends AbstractAction {
        private final Command command;

        private ExecuteAction(Command command) {
            super(command.getName());
            this.command = command;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            executeCommand(command);
        }
    }

    ArrayList<Command> getCommands() {
        return commands;
    }

    /**
     * Interface used to provide a file.
     */
    public interface FilenameProvider {
        /**
         * @return the file which is to create
         */
        File getCurrentFilename();
    }

    /**
     * Interface used to provide the circuit
     */
    public interface CircuitProvider {
        /**
         * @return the circuit which is to use
         */
        Circuit getCurrentCircuit();
    }

    /**
     * Interface used to provide the library.
     */
    public interface LibraryProvider {
        /**
         * @return the library which currently used
         */
        ElementLibrary getCurrentLibrary();
    }

    /**
     * Interface used to write a file
     */
    public interface IOInterface {

        /**
         * Creates an output stream
         *
         * @param filename the filename
         * @return the output stream
         * @throws IOException IOException
         */
        OutputStream getOutputStream(File filename) throws IOException;

        /**
         * Starts a process
         *
         * @param dir  the folder to start the process in
         * @param args the arguments
         * @throws IOException IOException
         */
        void startProcess(File dir, String[] args) throws IOException;

        /**
         * Shows an error message
         *
         * @param command the command that failed
         * @param e       the error
         */
        void showError(Command command, Exception e);
    }

    private static final class DefaultIOInterface implements IOInterface {

        @Override
        public OutputStream getOutputStream(File filename) throws IOException {
            final File parentFile = filename.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs())
                    throw new IOException("could not create "+parentFile);
            }
            return new FileOutputStream(filename);
        }

        @Override
        public void startProcess(File dir, String[] args) throws IOException {
            ProcessStarter.start(dir, args);
        }

        @Override
        public void showError(Command command, Exception e) {
            new ErrorMessage(Lang.get("msg_errorStartCommand_N", command.getName())).addCause(e).show();
        }
    }

    private static final class ModelAccess implements HGSMap {
        private final HDLCircuit hdlCircuit;

        private ModelAccess(HDLCircuit hdlCircuit) {
            this.hdlCircuit = hdlCircuit;
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            switch (key) {
                case "ports":
                    return new PortsArray(hdlCircuit.getPorts());
                default:
                    throw new HGSEvalException("field " + key + " not found!");
            }
        }
    }

    private static final class PortsArray implements HGSArray {
        private final ArrayList<HDLPort> ports;

        private PortsArray(ArrayList<HDLPort> ports) {
            this.ports = ports;
        }

        @Override
        public int hgsArraySize() {
            return ports.size();
        }

        @Override
        public Object hgsArrayGet(int i) {
            return new Port(ports.get(i));
        }
    }

    private static final class Port implements HGSMap {
        private final HDLPort hdlPort;

        private Port(HDLPort hdlPort) {
            this.hdlPort = hdlPort;
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            switch (key) {
                case "dir":
                    return hdlPort.getDirection().name();
                case "name":
                    return hdlPort.getName();
                case "bits":
                    return hdlPort.getBits();
                case "pin":
                    return hdlPort.getPinNumber();
                case "clock":
                    return hdlPort.isClock();
                default:
                    throw new HGSEvalException("field " + key + " not found!");
            }
        }
    }
}
