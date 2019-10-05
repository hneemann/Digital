/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.toolchain;

import com.thoughtworks.xstream.XStream;
import de.neemann.digital.XStreamValid;
import de.neemann.digital.builder.tt2.OSExecute;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.gui.SaveAsHelper;
import de.neemann.digital.gui.StatusInterface;
import de.neemann.digital.hdl.hgs.*;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.hdl.vhdl2.VHDLGenerator;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.language.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Used to create the IDE integration
 */
public final class Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);
    static final String LOOK_AT_ALIAS = "lookAt";
    static final String REF_ALIAS = "ref";

    /**
     * Loads a configuration
     *
     * @param file the file to load
     * @return the configuration
     * @throws IOException IOException
     */
    public static Configuration load(File file) throws IOException {
        final Configuration configuration = load(new FileInputStream(file));
        configuration.origin = file;
        return configuration;
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
        final XStream xStream = new XStreamValid();
        xStream.alias("toolchain", Configuration.class);
        xStream.aliasAttribute(Configuration.class, "name", "name");
        xStream.aliasAttribute(Configuration.class, "frequency", "frequency");
        xStream.aliasAttribute(Configuration.class, "clockGenerator", "clockGenerator");
        xStream.aliasAttribute(Configuration.class, "params", "params");
        xStream.registerConverter(new Resources.MapEntryConverter("param"));
        xStream.alias("command", Command.class);
        xStream.aliasAttribute(Command.class, "name", "name");
        xStream.aliasAttribute(Command.class, "requires", "requires");
        xStream.aliasAttribute(Command.class, "filter", "filter");
        xStream.aliasAttribute(Command.class, "timeout", "timeout");
        xStream.addImplicitCollection(Command.class, "args", "arg", String.class);
        xStream.alias("file", FileToCreate.class);
        xStream.aliasAttribute(FileToCreate.class, "name", "name");
        xStream.aliasAttribute(FileToCreate.class, "overwrite", "overwrite");
        xStream.aliasAttribute(FileToCreate.class, "filter", "filter");
        xStream.aliasAttribute(FileToCreate.class, "id", "id");
        xStream.aliasAttribute(FileToCreate.class, "referenceFilename", LOOK_AT_ALIAS);
        xStream.aliasAttribute(FileToCreate.class, "referenceId", REF_ALIAS);
        return xStream;
    }

    private String name;
    private int frequency;
    private String clockGenerator;
    private ArrayList<Command> commands;
    private ArrayList<FileToCreate> files;
    private Map<String, String> params;
    private transient FilenameProvider filenameProvider;
    private transient CircuitProvider circuitProvider;
    private transient LibraryProvider libraryProvider;
    private transient IOInterface ioInterface;
    private transient File origin;


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
     * @param statusInterface used to show the commands status.
     * @return the menu
     */
    public JMenu createMenu(StatusInterface statusInterface) {
        JMenu menu = new JMenu(name);
        for (Command c : commands)
            menu.add(new JMenuItem(new ExecuteAction(c, statusInterface)));
        return menu;
    }

    private final class ExecuteAction extends AbstractAction {
        private final Command command;
        private final StatusInterface statusInterface;

        private ExecuteAction(Command command, StatusInterface statusInterface) {
            super(command.getName());
            this.command = command;
            this.statusInterface = statusInterface;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            executeCommand(command, this, statusInterface);
        }
    }

    private Context createContext(File fileToExecute, HDLModel hdlModel, Command command) throws HGSEvalException {
        final Context context = new Context()
                .declareVar("path", fileToExecute.getPath())
                .declareVar("dir", fileToExecute.getParentFile())
                .declareVar("name", fileToExecute.getName())
                .declareVar("shortname", createShortname(fileToExecute.getName()));

        if (params != null)
            for (Map.Entry<String, String> e : params.entrySet())
                context.declareVar(e.getKey(), toHGLValue(e.getValue()));

        if (command.needsHDL()) {
            context.declareVar("hdl", command.getHDL());
            if (command.getHDL().equals("vhdl"))
                context.declareVar("extension", ".vhdl");
            else
                context.declareVar("extension", ".v");
        }

        if (clockGenerator != null)
            context.declareVar("clockGenerator", clockGenerator);

        if (hdlModel != null)
            context.declareVar("model", new ModelAccess(hdlModel.getMain()));
        return context;
    }

    private Object toHGLValue(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e1) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e2) {
                return value;
            }
        }
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

    private HDLModel writeHDL(String hdl, File digFile) throws IOException, HGSEvalException, ElementNotFoundException, PinException, NodeException {

        // Creates the simulation model to ensure the circuit is error free.
        new ModelCreator(circuitProvider.getCurrentCircuit(), libraryProvider.getCurrentLibrary())
                .createModel(false)
                .close();

        final int modelDefinedFrequency = getFrequency();
        final boolean modelHasClock = modelDefinedFrequency > 0;
        switch (hdl) {
            case "verilog":
                File verilogFile = SaveAsHelper.checkSuffix(digFile, "v");
                final CodePrinter verilogPrinter = new CodePrinter(getIoInterface().getOutputStream(verilogFile));
                try (VerilogGenerator vlog = new VerilogGenerator(libraryProvider.getCurrentLibrary(), verilogPrinter)) {
                    if (modelHasClock) {
                        if ((this.frequency > 1 || clockGenerator != null) && modelDefinedFrequency < Integer.MAX_VALUE)
                            vlog.setClockIntegrator(createClockIntegrator());
                    }
                    vlog.export(circuitProvider.getCurrentCircuit());
                    return vlog.getModel();
                }
            case "vhdl":
                File vhdlFile = SaveAsHelper.checkSuffix(digFile, "vhdl");
                final CodePrinter vhdlPrinter = new CodePrinter(getIoInterface().getOutputStream(vhdlFile));
                try (VHDLGenerator vlog = new VHDLGenerator(libraryProvider.getCurrentLibrary(), vhdlPrinter)) {
                    if (modelHasClock) {
                        if ((this.frequency > 1 || clockGenerator != null) && modelDefinedFrequency < Integer.MAX_VALUE)
                            vlog.setClockIntegrator(createClockIntegrator());
                    }
                    vlog.export(circuitProvider.getCurrentCircuit());
                    return vlog.getModel();
                }
            default:
                throw new IOException(Lang.get("err_hdlNotKnown_N", hdl));
        }
    }

    private ClockIntegratorGeneric createClockIntegrator() {
        return new ClockIntegratorGeneric(frequency == 0 ? 0 : 1000000000.0 / frequency)
                .setClockGenerator(clockGenerator);
    }

    /**
     * Executes the given command
     *
     * @param command the command
     */
    Thread executeCommand(Command command, Action action, StatusInterface statusInterface) {
        File digFile = filenameProvider.getCurrentFilename();
        if (digFile != null) {
            try {
                if (statusInterface != null)
                    statusInterface.setStatus(Lang.get("msg_commandStarted_N", name + " - " + command.getName()));
                HDLModel hdlModel;
                if (command.needsHDL())
                    hdlModel = writeHDL(command.getHDL(), digFile);
                else
                    hdlModel = null;

                if (action != null)
                    action.setEnabled(false);
                Thread t = new Thread(() -> {
                    try {
                        checkFilesToCreate(digFile, hdlModel, command);

                        String[] args = command.getArgs();
                        if (args != null) {
                            if (command.isFilter()) {
                                final int argCount = command.getArgs().length;
                                Context context = createContext(digFile, hdlModel, command);
                                for (int i = 0; i < argCount; i++) {
                                    context.clearOutput();
                                    new Parser(args[i]).parse().execute(context);
                                    args[i] = context.toString();
                                }
                            }
                            getIoInterface().startProcess(command, digFile.getParentFile(), args);
                        }
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> getIoInterface().showError(command, e));
                    } finally {
                        if (action != null)
                            SwingUtilities.invokeLater(() -> action.setEnabled(true));
                        if (statusInterface != null)
                            statusInterface.setStatus(Lang.get("msg_commandEnded_N", name + " - " + command.getName()));
                    }
                });
                t.setDaemon(true);
                t.start();
                return t;
            } catch (Exception e) {
                getIoInterface().showError(command, e);
                if (statusInterface != null)
                    statusInterface.setStatus(Lang.get("msg_commandEnded_N", name + " - " + command.getName()));
            }
        }
        return null;
    }

    private void checkFilesToCreate(File fileToExecute, HDLModel hdlModel, Command command) throws HGSEvalException, IOException, ParserException {
        Context context = createContext(fileToExecute, hdlModel, command);

        final boolean modelHasClock = getFrequency() > 0;

        if (files != null) {
            ConfigCache configCache = new ConfigCache(origin);
            for (FileToCreate f : files) {
                context.clearOutput();
                final String name = f.getName();
                if (name == null)
                    throw new IOException("no file name given!");
                Parser p = new Parser(name);
                p.parse().execute(context);
                File filename = new File(fileToExecute.getParent(), context.toString());

                // do not create clockGenerator hdl code if no clock is used in the circuit
                boolean skip = !modelHasClock
                        && clockGenerator != null
                        && removeSuffix(filename.getName()).equals(clockGenerator);

                if (!skip) {
                    if (f.isOverwrite() || !filename.exists())
                        createFile(filename, resolveFileContent(f, configCache), context);
                }
            }
        }
    }

    private String removeSuffix(String name) {
        int p = name.lastIndexOf('.');
        if (p < 0)
            return name;
        else
            return name.substring(0, p);
    }

    private void createFile(File filename, FileToCreate f, Context context) throws IOException, HGSEvalException, ParserException {
        LOGGER.info("create file " + filename);
        Parser p;
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


    private FileToCreate resolveFileContent(FileToCreate f, ConfigCache configCache) throws IOException {
        if (f.hasContent())
            return f;

        Configuration c = configCache.getConfig(f.getReferenceFilename());
        return c.getFileById(f.getReferenceId(), configCache);
    }

    FileToCreate getFileById(String referenceId, ConfigCache configCache) throws IOException {
        for (FileToCreate f : files)
            if (referenceId.equals(f.getId()))
                return resolveFileContent(f, configCache);
        throw new IOException("no file with id " + referenceId + " given");
    }

    ArrayList<Command> getCommands() {
        return commands;
    }

    private int getFrequency() throws HGSEvalException {
        List<VisualElement> l = circuitProvider.getCurrentCircuit().getElements(v -> v.equalsDescription(Clock.DESCRIPTION));
        if (l.isEmpty())
            return 0;
        if (l.size() > 1)
            throw new HGSEvalException(Lang.get("err_moreThanOneClockFound"));

        return l.get(0).getElementAttributes().get(Keys.FREQUENCY);
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
         * @param command the command started
         * @param dir     the folder to start the process in
         * @param args    the arguments
         * @throws IOException IOException
         */
        void startProcess(Command command, File dir, String[] args) throws IOException;

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
                    throw new IOException("could not create folder " + parentFile);
            }
            return new FileOutputStream(filename);
        }

        @Override
        public void startProcess(Command command, File dir, String[] args) throws IOException {
            String consoleOut = new OSExecute(args)
                    .setTimeOutSec(command.getTimeout())
                    .setWorkingDir(dir)
                    .startAndWait();
            LOGGER.info("process '" + command.getName() + "' says:\n" + consoleOut);
        }

        @Override
        public void showError(Command command, Exception e) {
            new ErrorMessage(Lang.get("msg_errorStartCommand_N", command.getName())).addCause(e).show();
        }
    }

    private final class ModelAccess implements HGSMap {
        private final HDLCircuit hdlCircuit;

        private ModelAccess(HDLCircuit hdlCircuit) {
            this.hdlCircuit = hdlCircuit;
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            switch (key) {
                case "ports":
                    return new PortsArray(hdlCircuit.getPorts());
                case "frequency":
                    return getFrequency();
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
