/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.SubstituteLibrary;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.Button;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.memory.ProgramCounter;
import de.neemann.digital.core.stats.Statistics;
import de.neemann.digital.core.wiring.AsyncSeq;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.gif.GifExporter;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.ElementTypeDescriptionCustom;
import de.neemann.digital.draw.model.AsyncSequentialClock;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.fsm.gui.FSMFrame;
import de.neemann.digital.gui.components.*;
import de.neemann.digital.gui.components.data.GraphDialog;
import de.neemann.digital.gui.components.expression.ExpressionDialog;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.gui.components.modification.ModifyMeasurementOrdering;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.terminal.Keyboard;
import de.neemann.digital.gui.components.terminal.KeyboardDialog;
import de.neemann.digital.gui.components.testing.TestAllDialog;
import de.neemann.digital.gui.components.testing.ValueTableDialog;
import de.neemann.digital.gui.components.tree.LibraryTreeModel;
import de.neemann.digital.gui.components.tree.SelectTree;
import de.neemann.digital.gui.tutorial.InitialTutorial;
import de.neemann.digital.gui.release.CheckForNewRelease;
import de.neemann.digital.gui.remote.DigitalHandler;
import de.neemann.digital.gui.remote.RemoteException;
import de.neemann.digital.gui.remote.RemoteSever;
import de.neemann.digital.gui.state.State;
import de.neemann.digital.gui.state.StateManager;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.VerilogGenerator;
import de.neemann.digital.hdl.vhdl2.VHDLGenerator;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.digital.toolchain.Configuration;
import de.neemann.digital.undo.ChangedListener;
import de.neemann.digital.undo.Modifications;
import de.neemann.gui.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.gui.ToolTipAction.getCTRLMask;
import static javax.swing.JOptionPane.showInputDialog;

/**
 * The main frame of the Digital Simulator
 * Set log level: -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
 */
public final class Main extends JFrame implements ClosingWindowListener.ConfirmSave, FileHistory.OpenInterface, DigitalRemoteInterface, StatusInterface, ChangedListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String KEY_START_STOP_ACTION = "startStop";
    private static boolean experimental;

    /**
     * @return true if experimental features are enabled
     */
    public static boolean isExperimentalMode() {
        return experimental;
    }

    private static final String MESSAGE = Lang.get("message");
    private static final Icon ICON_RUN = IconCreator.create("media-playback-start.png");
    private static final Icon ICON_MICRO = IconCreator.create("media-playback-start-2.png");
    private static final Icon ICON_TEST = IconCreator.create("media-playback-start-T.png");
    private static final Icon ICON_STEP = IconCreator.create("media-seek-forward.png");
    private static final Icon ICON_STEP_FINISH = IconCreator.create("media-seek-forward-f.png");
    private static final Icon ICON_STOP = IconCreator.create("media-playback-stop.png");
    private static final Icon ICON_NEW = IconCreator.create("document-new.png");
    private static final Icon ICON_NEW_SUB = IconCreator.create("document-new-sub.png");
    private static final Icon ICON_OPEN = IconCreator.create("document-open.png");
    private static final Icon ICON_OPEN_WIN = IconCreator.create("document-open-new.png");
    private static final Icon ICON_SAVE = IconCreator.create("document-save.png");
    private static final Icon ICON_SAVE_AS = IconCreator.create("document-save-as.png");
    private static final Icon ICON_FAST = IconCreator.create("media-skip-forward.png");
    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOM_IN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOM_OUT = IconCreator.create("View-zoom-out.png");
    private static final Icon ICON_HELP = IconCreator.create("help.png");

    private final CircuitComponent circuitComponent;
    private final CircuitScrollPanel circuitScrollPanel;
    private final ToolTipAction save;
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final JLabel statusLabel;
    private final StateManager stateManager = new StateManager();
    private final ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(1);
    private final WindowPosManager windowPosManager;
    private final InsertHistory insertHistory;
    private final boolean keepPrefMainFile;
    private final FileHistory fileHistory;

    private ToolTipAction doMicroStep;
    private ToolTipAction runToBreakMicroAction;
    private ToolTipAction runToBreakAction;
    private ToolTipAction showMeasurementDialog;
    private ToolTipAction showMeasurementGraph;
    private ToolTipAction runTests;

    private File baseFilename;
    private File filename;
    private boolean modifiedPrefixVisible = false;

    private Model model;

    private ModelCreator modelCreator;
    private boolean realTimeClockRunning;

    private State stoppedState;
    private RunModelState runModelState;
    private State runModelMicroState;
    private JComponent componentOnPane;
    private LibraryTreeModel treeModel;

    /**
     * Creates a new instance
     *
     * @param builder the builder
     */
    private Main(MainBuilder builder) {
        super(Lang.get("digital"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImages(IconCreator.createImages("icon32.png", "icon64.png", "icon128.png"));

        windowPosManager = new WindowPosManager(this);

        keepPrefMainFile = builder.keepPrefMainFile;

        if (builder.library != null) library = builder.library;
        else {
            library = new ElementLibrary(Settings.getInstance().get(Keys.SETTINGS_JAR_PATH));
            Exception e = library.checkForException();
            if (e != null)
                SwingUtilities.invokeLater(new ErrorMessage(Lang.get("err_loadingLibrary")).addCause(e).setComponent(this));
        }

        shapeFactory = new ShapeFactory(library, Settings.getInstance().get(Keys.SETTINGS_IEEE_SHAPES));

        fileHistory = new FileHistory(this);

        baseFilename = builder.baseFileName;

        circuitComponent = new CircuitComponent(this, library, shapeFactory);
        circuitComponent.addListener(this);
        if (builder.circuit != null) {
            SwingUtilities.invokeLater(() -> circuitComponent.setCircuit(builder.circuit));
            setFilename(builder.fileToOpen, false);
        } else {
            if (builder.fileToOpen != null) {
                SwingUtilities.invokeLater(() -> loadFile(builder.fileToOpen, builder.library == null, builder.library == null));
            } else {
                File name = fileHistory.getMostRecent();
                if (name != null) {
                    SwingUtilities.invokeLater(() -> loadFile(name, true, false));
                }
            }
        }
        circuitScrollPanel = new CircuitScrollPanel(circuitComponent);

        library.addListener(circuitComponent);

        getContentPane().add(circuitScrollPanel);
        componentOnPane = circuitScrollPanel;

        circuitComponent.addKeyListener(new ModelKeyListener());

        statusLabel = new JLabel(" ");
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        setupStates();

        JMenuBar menuBar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        save = createFileMenu(menuBar, toolBar, builder.allowAllFileActions);
        toolBar.addSeparator();

        createEditMenu(menuBar);
        createViewMenu(menuBar, toolBar);

        toolBar.addSeparator();

        toolBar.add(circuitComponent.getUndoAction().createJButtonNoText());
        toolBar.add(circuitComponent.getRedoAction().createJButtonNoText());
        toolBar.add(circuitComponent.getDeleteAction().createJButtonNoText());
        toolBar.addSeparator();

        createStartMenu(menuBar, toolBar);

        createAnalyseMenu(menuBar);

        toolBar.addSeparator();

        insertHistory = new InsertHistory(toolBar, library);
        library.addListener(insertHistory);
        final LibrarySelector librarySelector = new LibrarySelector(library, shapeFactory);
        library.addListener(librarySelector);
        menuBar.add(librarySelector.buildMenu(insertHistory, circuitComponent));

        menuBar.add(WindowManager.getInstance().registerAndCreateMenu(this));

        JMenu helpMenu = new JMenu(Lang.get("menu_help"));
        helpMenu.add(new ToolTipAction(Lang.get("menu_help_elements")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new ElementHelpDialog(Main.this, library, shapeFactory).setVisible(true);
                } catch (NodeException | PinException e) {
                    new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_help_elements_tt")).createJMenuItem());
        new DocumentationLocator().addMenuTo(helpMenu);
        helpMenu.addSeparator();
        helpMenu.add(InfoDialog.getInstance().createMenuItem(this, MESSAGE));
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        addWindowListener(new ClosingWindowListener(this, this));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                clearModelDescription(); // stop model timer if running
                timerExecutor.shutdown();
                library.removeListener(librarySelector);
                library.removeListener(insertHistory);
                library.removeListener(circuitComponent);
                if (treeModel != null)
                    library.removeListener(treeModel);
                windowPosManager.shutdown();
            }
        });

        getContentPane().add(toolBar, BorderLayout.NORTH);

        new ToolTipAction("insertLast") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                InsertAction lastInsertAction = insertHistory.getLastInsertAction();
                if (lastInsertAction != null && model == null)
                    lastInsertAction.actionPerformed(actionEvent);
            }
        }.setAccelerator("L").enableAcceleratorIn(circuitComponent);

        enableClockShortcut();

        new WindowSizeStorage(builder.mainFrame ? "main" : "sub").restore(this);

        if (builder.parent != null) {
            Point p = builder.parent.getLocation();
            final float d = 20 * Screen.getInstance().getScaling();
            p.x += d;
            p.y += d;
            Screen.setLocation(this, p, false);
        } else
            setLocationRelativeTo(null);

        checkIDEIntegration(builder, menuBar);
    }

    private void checkIDEIntegration(MainBuilder builder, JMenuBar menuBar) {
        if (builder.mainFrame) {
            File f = Settings.getInstance().get(Keys.SETTINGS_TOOLCHAIN_CONFIG);
            if (f.getPath().length() > 0) {
                try {
                    menuBar.add(
                            Configuration.load(f)
                                    .setCircuitProvider(() -> getCircuitComponent().getCircuit())
                                    .setFilenameProvider(() -> {
                                        saveChanges();
                                        return filename;
                                    })
                                    .setLibraryProvider(() -> library)
                                    .createMenu(this));
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorReadingToolchainConfig_N", f.getPath())).addCause(e).setComponent(this));
                }
            }
        }
    }

    private void enableClockShortcut() {
        new ToolTipAction("doClock") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (model != null && !realTimeClockRunning) {
                    ArrayList<Clock> cl = model.getClocks();
                    if (cl.size() == 1) {
                        model.modify(() -> {
                            ObservableValue clkVal = cl.get(0).getClockOutput();
                            clkVal.setBool(!clkVal.getBool());
                        });
                    }
                }
            }
        }.setAccelerator("C").enableAcceleratorIn(circuitComponent);
    }

    private void createViewMenu(JMenuBar menuBar, JToolBar toolBar) {
        ToolTipAction maximize = new ToolTipAction(Lang.get("menu_maximize"), ICON_EXPAND) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.fitCircuit();
            }
        }.setAccelerator("F1");
        ToolTipAction zoomIn = new ToolTipAction(Lang.get("menu_zoomIn"), ICON_ZOOM_IN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.scaleCircuit(1 / 0.9);
            }
        }.setAcceleratorCTRLplus("PLUS").enableAcceleratorIn(circuitComponent);
        circuitComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, getCTRLMask()), zoomIn);

        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOM_OUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.scaleCircuit(0.9);
            }
        }.setAcceleratorCTRLplus("MINUS").enableAcceleratorIn(circuitComponent);
        circuitComponent.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, getCTRLMask()), zoomOut);

        ToolTipAction viewHelp = new ToolTipAction(Lang.get("menu_viewHelp"), ICON_HELP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Circuit circuit = circuitComponent.getCircuit();
                final String name = Lang.get("msg_actualCircuit");
                File file = filename;
                if (file == null)
                    file = new File(name);
                try {
                    ElementTypeDescriptionCustom description =
                            ElementLibrary.createCustomDescription(file, circuit, library);
                    description.setShortName(name);
                    description.setDescription(Lang.evalMultilingualContent(circuit.getAttributes().get(Keys.DESCRIPTION)));
                    new ElementHelpDialog(Main.this, description, circuit.getAttributes()).setVisible(true);
                } catch (PinException | NodeException e1) {
                    new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e1).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_viewHelp_tt"));

        JCheckBoxMenuItem treeCheckBox = new JCheckBoxMenuItem(Lang.get("menu_treeSelect"));
        treeCheckBox.setToolTipText(Lang.get("menu_treeSelect_tt"));
        treeCheckBox.addActionListener(actionEvent -> {
            getContentPane().remove(componentOnPane);
            if (treeCheckBox.isSelected()) {
                JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                treeModel = new LibraryTreeModel(library);
                split.setLeftComponent(new JScrollPane(new SelectTree(treeModel, circuitComponent, shapeFactory, insertHistory)));
                split.setRightComponent(circuitScrollPanel);
                getContentPane().add(split);
                componentOnPane = split;
            } else {
                if (treeModel != null) {
                    library.removeListener(treeModel);
                    treeModel = null;
                }
                getContentPane().add(circuitScrollPanel);
                componentOnPane = circuitScrollPanel;
            }
            revalidate();
        });
        treeCheckBox.setAccelerator(KeyStroke.getKeyStroke("F5"));

        ToolTipAction tutorial = new ToolTipAction(Lang.get("menu_tutorial")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    clearPane();
                    new InitialTutorial(Main.this).setVisible(true);
                }
            }
        }.setToolTip(Lang.get("menu_tutorial_tt"));

        if (Settings.getInstance().get(Keys.SETTINGS_DEFAULT_TREESELECT))
            SwingUtilities.invokeLater(treeCheckBox::doClick);

        toolBar.add(viewHelp.createJButtonNoText());
        toolBar.add(zoomIn.createJButtonNoText());
        toolBar.add(zoomOut.createJButtonNoText());
        toolBar.add(maximize.createJButtonNoText());

        JMenu view = new JMenu(Lang.get("menu_view"));
        menuBar.add(view);
        view.add(maximize.createJMenuItem());
        view.add(zoomOut.createJMenuItem());
        view.add(zoomIn.createJMenuItem());
        view.addSeparator();
        view.add(treeCheckBox);
        view.addSeparator();
        view.add(tutorial.createJMenuItem());
        view.addSeparator();
        view.add(viewHelp.createJMenuItem());
    }

    private void clearPane() {
        circuitComponent.setCircuit(new Circuit());
        setFilename(null, true);
        windowPosManager.closeAll();
        try {
            library.setRootFilePath(null);
        } catch (IOException e1) {
            // can not happen, no folder is scanned
        }
    }

    /**
     * Creates the file menu and adds it to menu and toolbar
     *
     * @param menuBar the menuBar
     * @param toolBar the toolBar
     * @return the save action
     */
    private ToolTipAction createFileMenu(JMenuBar menuBar, JToolBar toolBar, boolean allowAll) {
        ToolTipAction newFile = new ToolTipAction(Lang.get("menu_new"), ICON_NEW) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    clearPane();
                }
            }
        }.setAcceleratorCTRLplus('N').setToolTip(Lang.get("menu_new_tt")).setEnabledChain(allowAll);

        ToolTipAction newSubFile = new ToolTipAction(Lang.get("menu_newSub"), ICON_NEW_SUB) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new MainBuilder()
                        .setParent(Main.this)
                        .setLibrary(library)
                        .setCircuit(new Circuit())
                        .setBaseFileName(getBaseFileName())
                        .keepPrefMainFile()
                        .build()
                        .setVisible(true);
            }
        }.setToolTip(Lang.get("menu_newSub_tt"));

        ToolTipAction open = new ToolTipAction(Lang.get("menu_open"), ICON_OPEN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    JFileChooser fc = getJFileChooser(baseFilename);
                    if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                        loadFile(fc.getSelectedFile(), true, true);
                    }
                }
            }
        }.setAcceleratorCTRLplus('O').setEnabledChain(allowAll);

        ToolTipAction openWin = new ToolTipAction(Lang.get("menu_openWin"), ICON_OPEN_WIN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getJFileChooser(baseFilename);
                if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    new MainBuilder()
                            .setParent(Main.this)
                            .setFileToOpen(fc.getSelectedFile())
                            .build()
                            .setVisible(true);
                }
            }
        }.setToolTip(Lang.get("menu_openWin_tt")).setEnabledChain(allowAll);

        JMenu openRecent = new JMenu(Lang.get("menu_openRecent"));
        JMenu openRecentNewWindow = new JMenu(Lang.get("menu_openRecentNewWindow"));
        fileHistory.setMenu(openRecent, openRecentNewWindow);
        openRecent.setEnabled(allowAll);
        openRecentNewWindow.setEnabled(allowAll);

        ToolTipAction saveAs = new ToolTipAction(Lang.get("menu_saveAs"), ICON_SAVE_AS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getJFileChooser(baseFilename);
                final SaveAsHelper saveAsHelper = new SaveAsHelper(Main.this, fc, "dig");
                saveAsHelper.checkOverwrite(
                        file -> {
                            if (library.isFileAccessible(file))
                                saveFile(file, !keepPrefMainFile);
                            else {
                                Object[] options = {Lang.get("btn_saveAnyway"), Lang.get("btn_newName"), Lang.get("cancel")};
                                int res = JOptionPane.showOptionDialog(Main.this,
                                        Lang.get("msg_fileNotAccessible"),
                                        Lang.get("msg_warning"),
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                                        null, options, options[0]);
                                switch (res) {
                                    case 0:
                                        saveFile(file, true);
                                        library.setRootFilePath(file.getParentFile());
                                        if (library.getWarningMessage() != null)
                                            SwingUtilities.invokeLater(new ErrorMessage(library.getWarningMessage().toString()).setComponent(Main.this));
                                        break;
                                    case 1:
                                        saveAsHelper.retryFileSelect();
                                }
                            }
                        }
                );
            }
        };

        ToolTipAction save = new ToolTipAction(Lang.get("menu_save"), ICON_SAVE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveAs.actionPerformed(e);
                else
                    saveFile(filename, false);
            }
        }.setAcceleratorCTRLplus('S').setEnabledChain(false);

        JMenu export = new JMenu(Lang.get("menu_export"));
        export.add(new ExportAction(Lang.get("menu_exportSVG"), "svg", GraphicSVG::new));
        export.add(new ToolTipAction(Lang.get("menu_exportSVGSettings")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ElementAttributes modified = new AttributeDialog(Main.this, SVGSettings.getInstance().getKeys(), SVGSettings.getInstance().getAttributes()).showDialog();
                SVGSettings.getInstance().getAttributes().getValuesFrom(modified);
            }
        });
        export.addSeparator();
        export.add(new ExportAction(Lang.get("menu_exportPNGSmall"), "png", (out) -> new GraphicsImage(out, "PNG", 1)));
        export.add(new ExportAction(Lang.get("menu_exportPNGLarge"), "png", (out) -> new GraphicsImage(out, "PNG", 2)));

        if (isExperimentalMode())
            export.add(new ExportGifAction(Lang.get("menu_exportAnimatedGIF")));

        export.addSeparator();

        export.add(createVHDLExportAction().createJMenuItem());
        export.add(createVerilogExportAction().createJMenuItem());

        export.addSeparator();
        export.add(new ExportZipAction(this).createJMenuItem());

        JMenu file = new JMenu(Lang.get("menu_file"));
        menuBar.add(file);
        file.add(newFile.createJMenuItem());
        file.add(newSubFile.createJMenuItem());
        file.add(open.createJMenuItem());
        file.add(openRecent);
        file.add(openWin.createJMenuItem());
        file.add(openRecentNewWindow);
        file.add(save.createJMenuItem());
        file.add(saveAs.createJMenuItem());
        file.add(export);

        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());

        return save;
    }

    private ToolTipAction createVHDLExportAction() {
        return new ToolTipAction(Lang.get("menu_exportVHDL")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // check model for errors
                try {
                    new ModelCreator(circuitComponent.getCircuit(), library).createModel(false).close();
                } catch (PinException | NodeException | ElementNotFoundException | RuntimeException e) {
                    showError(Lang.get("msg_modelHasErrors"), e);
                    return;
                }

                JFileChooser fc = new MyFileChooser();
                if (filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(filename, "vhdl"));

                ElementAttributes settings = Settings.getInstance().getAttributes();
                File exportDir = settings.getFile("exportDirectory");
                if (exportDir != null)
                    fc.setCurrentDirectory(exportDir);

                fc.addChoosableFileFilter(new FileNameExtensionFilter("VHDL", "vhdl"));
                new SaveAsHelper(Main.this, fc, "vhdl").checkOverwrite(
                        file -> {
                            settings.setFile("exportDirectory", file.getParentFile());
                            try (VHDLGenerator vhdl = new VHDLGenerator(library, new CodePrinter(file))) {
                                vhdl.export(circuitComponent.getCircuit());
                            }
                        }
                );
            }
        }.setToolTip(Lang.get("menu_exportVHDL_tt"));
    }

    private ToolTipAction createVerilogExportAction() {
        return new ToolTipAction(Lang.get("menu_exportVerilog")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                // check model for errors
                try {
                    new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
                } catch (PinException | NodeException | ElementNotFoundException e) {
                    showError(Lang.get("msg_modelHasErrors"), e);
                    return;
                }

                JFileChooser fc = new MyFileChooser();
                if (filename != null)
                    fc.setSelectedFile(SaveAsHelper.checkSuffix(filename, "v"));

                ElementAttributes settings = Settings.getInstance().getAttributes();
                File exportDir = settings.getFile("exportDirectory");
                if (exportDir != null)
                    fc.setCurrentDirectory(exportDir);

                fc.addChoosableFileFilter(new FileNameExtensionFilter("Verilog", "v"));
                new SaveAsHelper(Main.this, fc, "v").checkOverwrite(
                        file -> {
                            settings.setFile("exportDirectory", file.getParentFile());
                            try (VerilogGenerator vlog = new VerilogGenerator(library, new CodePrinter(file))) {
                                vlog.export(circuitComponent.getCircuit());
                            }
                        }
                );
            }
        }.setToolTip(Lang.get("menu_exportVerilog_tt"));
    }

    /**
     * @return the file name base
     */
    public File getBaseFileName() {
        if (filename != null) return filename;
        return baseFilename;
    }

    /**
     * @return the circuit component
     */
    public CircuitComponent getCircuitComponent() {
        return circuitComponent;
    }

    /**
     * Creates the edit menu
     *
     * @param menuBar the menu bar
     */
    private void createEditMenu(JMenuBar menuBar) {
        JMenu edit = new JMenu(Lang.get("menu_edit"));
        menuBar.add(edit);

        ToolTipAction orderInputs = new ToolTipAction(Lang.get("menu_orderInputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementOrder o = new ElementOrder(circuitComponent,
                        element -> element.equalsDescription(In.DESCRIPTION)
                                || element.equalsDescription(Clock.DESCRIPTION),
                        Lang.get("menu_orderInputs"));
                if (new ElementOrderer<>(Main.this, Lang.get("menu_orderInputs"), o).addOkButton().showDialog())
                    circuitComponent.modify(o.getModifications());
            }
        }.setToolTip(Lang.get("menu_orderInputs_tt"));

        ToolTipAction orderOutputs = new ToolTipAction(Lang.get("menu_orderOutputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementOrder o = new ElementOrder(circuitComponent,
                        element -> element.equalsDescription(Out.DESCRIPTION)
                                || element.equalsDescription(Out.LEDDESCRIPTION),
                        Lang.get("menu_orderOutputs"));
                if (new ElementOrderer<>(Main.this, Lang.get("menu_orderOutputs"), o).addOkButton().showDialog())
                    circuitComponent.modify(o.getModifications());
            }
        }.setToolTip(Lang.get("menu_orderOutputs_tt"));

        ToolTipAction orderMeasurements = new ToolTipAction(Lang.get("menu_orderMeasurements")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                orderMeasurements();
            }
        }.setToolTip(Lang.get("menu_orderMeasurements_tt"));


        ToolTipAction editAttributes = new ToolTipAction(Lang.get("menu_editAttributes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.editCircuitAttributes();
            }
        }.setToolTip(Lang.get("menu_editAttributes_tt"));

        ToolTipAction editSettings = new ToolTipAction(Lang.get("menu_editSettings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementAttributes modified =
                        new AttributeDialog(Main.this, Settings.getInstance().getKeys(), Settings.getInstance().getAttributes())
                                .setDialogTitle(Lang.get("menu_editSettings"))
                                .showDialog();
                if (modified != null) {
                    FormatToExpression.setDefaultFormat(modified.get(Keys.SETTINGS_EXPRESSION_FORMAT));
                    ColorScheme.updateCustomColorScheme(modified);

                    if (Settings.getInstance().requiresRestart(modified)) {
                        Lang.setLanguage(modified.get(Keys.SETTINGS_LANGUAGE));
                        JOptionPane.showMessageDialog(Main.this, Lang.get("msg_restartNeeded"));
                    }
                    if (Settings.getInstance().requiresRepaint(modified))
                        circuitComponent.graphicHasChanged();

                    Settings.getInstance().getAttributes().getValuesFrom(modified);
                }
            }
        }.setToolTip(Lang.get("menu_editSettings_tt"));


        ToolTipAction actualToDefault = new ToolTipAction(Lang.get("menu_actualToDefault")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.actualToDefault();
                ensureModelIsStopped();
            }
        }.setToolTip(Lang.get("menu_actualToDefault_tt"));

        ToolTipAction restoreAllFuses = new ToolTipAction(Lang.get("menu_restoreAllFuses")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.restoreAllFuses();
                ensureModelIsStopped();
            }
        }.setToolTip(Lang.get("menu_restoreAllFuses_tt"));

        ToolTipAction insertAsNew = new ToolTipAction(Lang.get("menu_insertAsNew")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    Object data = clipboard.getData(DataFlavor.stringFlavor);
                    if (data instanceof String) {
                        ArrayList<Movable> elements = CircuitTransferable.createList(data, shapeFactory);
                        if (elements != null) {
                            Circuit circuit = new Circuit();
                            for (Movable m : elements) {
                                if (m instanceof Wire)
                                    circuit.add((Wire) m);
                                if (m instanceof VisualElement)
                                    circuit.add((VisualElement) m);
                            }

                            new MainBuilder()
                                    .setParent(Main.this)
                                    .setLibrary(library)
                                    .setCircuit(circuit)
                                    .setBaseFileName(getBaseFileName())
                                    .keepPrefMainFile()
                                    .build()
                                    .setVisible(true);
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_clipboardContainsNoImportableData")).setComponent(Main.this));
                }
            }
        }.setToolTip(Lang.get("menu_insertAsNew_tt"));

        ToolTipAction labelPins = new ToolTipAction(Lang.get("menu_labelPins")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.labelPins();
                ensureModelIsStopped();
            }
        }.setToolTip(Lang.get("menu_labelPins_tt"));

        edit.add(circuitComponent.getUndoAction().createJMenuItemNoIcon());
        edit.add(circuitComponent.getRedoAction().createJMenuItemNoIcon());
        edit.addSeparator();
        edit.add(editAttributes.createJMenuItem());
        edit.add(actualToDefault.createJMenuItem());
        edit.add(restoreAllFuses.createJMenuItem());
        edit.add(createSpecialEditMenu());
        edit.add(labelPins.createJMenuItem());
        edit.addSeparator();
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());
        edit.add(orderMeasurements.createJMenuItem());
        edit.addSeparator();

        edit.add(circuitComponent.getCutAction().createJMenuItem());
        edit.add(circuitComponent.getCopyAction().createJMenuItem());
        edit.add(circuitComponent.getPasteAction().createJMenuItem());
        edit.add(circuitComponent.getRotateAction().createJMenuItem());
        edit.add(insertAsNew.createJMenuItem());
        edit.addSeparator();
        edit.add(editSettings.createJMenuItem());
    }

    private JMenu createSpecialEditMenu() {
        JMenu special = new JMenu(Lang.get("menu_special"));
        special.add(new ToolTipAction(Lang.get("menu_addPrefix")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!circuitComponent.isLocked()) {
                    String prefix = showInputDialog(Lang.get("menu_addPrefix"));
                    if (prefix != null && prefix.length() > 0) {
                        Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_addPrefix"));
                        for (Drawable d : circuitComponent.getHighLighted()) {
                            if (d instanceof VisualElement) {
                                VisualElement v = (VisualElement) d;
                                if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Out.DESCRIPTION)) {
                                    ElementAttributes attr = v.getElementAttributes();
                                    String l = prefix + attr.getLabel();
                                    builder.add(new ModifyAttribute<>(v, Keys.LABEL, l));
                                }
                            }
                        }
                        circuitComponent.modify(builder.build());
                    }
                }
            }
        }.setToolTip(Lang.get("menu_addPrefix_tt")).createJMenuItem());
        special.add(new ToolTipAction(Lang.get("menu_removePrefix")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!circuitComponent.isLocked()) {
                    Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_removePrefix"));
                    for (Drawable d : circuitComponent.getHighLighted()) {
                        if (d instanceof VisualElement) {
                            VisualElement v = (VisualElement) d;
                            if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Out.DESCRIPTION)) {
                                ElementAttributes attr = v.getElementAttributes();
                                String l = attr.getLabel();
                                if (l.length() > 1)
                                    builder.add(new ModifyAttribute<>(v, Keys.LABEL, l.substring(1)));
                            }
                        }
                    }
                    circuitComponent.modify(builder.build());
                }
            }
        }.setToolTip(Lang.get("menu_removePrefix_tt")).createJMenuItem());
        special.add(new ToolTipAction(Lang.get("menu_numbering")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!circuitComponent.isLocked())
                    new NumberingWizard(Main.this, circuitComponent).start();
            }
        }.setToolTip(Lang.get("menu_numbering_tt")).createJMenuItem());
        special.add(new ToolTipAction(Lang.get("menu_removePinNumbers")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!circuitComponent.isLocked()) {
                    Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_removePinNumbers"));
                    for (VisualElement v : circuitComponent.getCircuit().getElements()) {
                        if (v.equalsDescription(In.DESCRIPTION)
                                || v.equalsDescription(Clock.DESCRIPTION)
                                || v.equalsDescription(Out.DESCRIPTION)) {
                            ElementAttributes attr = v.getElementAttributes();
                            String p = attr.get(Keys.PINNUMBER);
                            if (p.length() > 0)
                                builder.add(new ModifyAttribute<>(v, Keys.PINNUMBER, ""));
                        }
                    }
                    circuitComponent.modify(builder.build());
                }
            }
        }.setToolTip(Lang.get("menu_removePinNumbers_tt")).createJMenuItem());

        special.add(new ToolTipAction(Lang.get("menu_addPowerSupply")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (!circuitComponent.isLocked()) {
                    int maxNum = 0;
                    HashSet<Integer> pinsUsed = new HashSet<>();
                    for (VisualElement v : circuitComponent.getCircuit().getElements()) {
                        if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Out.DESCRIPTION)) {
                            int pin = v.getElementAttributes().getIntPinNumber();
                            maxNum = Math.max(maxNum, pin);
                            pinsUsed.add(pin);
                        }
                    }
                    int guessedVCC = 0;
                    int guessedGND = 0;
                    if ((maxNum & 1) != 0) {
                        maxNum++;
                        guessedVCC = maxNum;
                        guessedGND = maxNum / 2;
                    }

                    if (pinsUsed.contains(guessedGND) || pinsUsed.contains(guessedVCC)) {
                        guessedGND = 0;
                        guessedVCC = 0;
                    }

                    // defines the power supply circuit
                    ArrayList<Movable> list = new ArrayList<>();
                    list.add(new VisualElement(PowerSupply.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setPos(new Vector(SIZE * 2, 0)));
                    VisualElement vcc = new VisualElement(In.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setAttribute(Keys.LABEL, "VCC")
                            .setAttribute(Keys.INPUT_DEFAULT, new InValue(1))
                            .setPos(new Vector(0, 0));
                    if (guessedVCC > 0)
                        vcc.setAttribute(Keys.PINNUMBER, Integer.toString(guessedVCC));

                    list.add(vcc);
                    VisualElement gnd = new VisualElement(In.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setAttribute(Keys.LABEL, "GND")
                            .setPos(new Vector(0, SIZE * 2));
                    if (guessedGND > 0)
                        gnd.setAttribute(Keys.PINNUMBER, Integer.toString(guessedGND));

                    list.add(gnd);
                    list.add(new Wire(new Vector(0, 0), new Vector(SIZE * 2, 0)));
                    list.add(new Wire(new Vector(0, SIZE * 2), new Vector(SIZE, SIZE * 2)));
                    list.add(new Wire(new Vector(SIZE, SIZE * 2), new Vector(SIZE, SIZE)));
                    list.add(new Wire(new Vector(SIZE, SIZE), new Vector(SIZE * 2, SIZE)));
                    circuitComponent.setPartsToInsert(list, null);
                }
            }
        }.setToolTip(Lang.get("menu_addPowerSupply_tt")).createJMenuItem());

        return special;
    }

    /**
     * Creates the start menu
     *
     * @param menuBar the menu bar
     * @param toolBar the tool bar
     */
    private void createStartMenu(JMenuBar menuBar, JToolBar toolBar) {
        doMicroStep = new ToolTipAction(Lang.get("menu_step"), ICON_STEP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.doMicroStep(false);
            }
        }.setToolTip(Lang.get("menu_step_tt")).setAccelerator("V").setEnabledChain(false);
        runToBreakMicroAction = new ToolTipAction(Lang.get("menu_runToBreakMicro"), ICON_STEP_FINISH) {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.runToBreakMicro();
            }
        }.setToolTip(Lang.get("menu_runToBreakMicro_tt")).setAccelerator("B").setEnabledChain(false);

        ToolTipAction runModelAction = runModelState.createToolTipAction(Lang.get("menu_run"), ICON_RUN)
                .setToolTip(Lang.get("menu_run_tt"));
        ToolTipAction runModelMicroAction = runModelMicroState.createToolTipAction(Lang.get("menu_micro"), ICON_MICRO)
                .setToolTip(Lang.get("menu_micro_tt"));
        runToBreakAction = new ToolTipAction(Lang.get("menu_fast"), ICON_FAST) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Model.BreakInfo info = model.runToBreak();
                if (info != null)
                    statusLabel.setText(Lang.get("stat_clocks", info.getSteps(), info.getLabel()));
            }
        }.setToolTip(Lang.get("menu_fast_tt")).setEnabledChain(false).setAccelerator("F7");

        ToolTipAction stoppedStateAction = stoppedState
                .createToolTipAction(Lang.get("menu_element"), ICON_STOP)
                .setToolTip(Lang.get("menu_element_tt"))
                .setEnabledChain(false);

        runTests = new ToolTipAction(Lang.get("menu_runTests"), ICON_TEST) {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTests();
            }
        }.setToolTip(Lang.get("menu_runTests_tt")).setAccelerator("F8");

        ToolTipAction runAllTests = new ToolTipAction(Lang.get("menu_runAllTests")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename != null)
                    new TestAllDialog(Main.this, filename.getParentFile(), shapeFactory, library).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_runAllTests_tt")).setAccelerator("F11");

        ToolTipAction speedTest = new ToolTipAction(Lang.get("menu_speedTest")) {
            private final NumberFormat format = new DecimalFormat("0.0");

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
                    try {
                        model.setWindowPosManager(windowPosManager);
                        SpeedTest speedTest = new SpeedTest(model);
                        String frequency = format.format(speedTest.calculate() / 1000);
                        circuitComponent.getCircuit().clearState();
                        SwingUtilities.invokeLater(() -> {
                            windowPosManager.closeAll();
                            JOptionPane.showMessageDialog(Main.this, Lang.get("msg_frequency_N", frequency));
                        });
                    } finally {
                        model.close();
                    }
                } catch (Exception e1) {
                    showError(Lang.get("msg_speedTestError"), e1);
                }
            }
        }.setToolTip(Lang.get("menu_speedTest_tt"));

        showMeasurementDialog = new ToolTipAction(Lang.get("menu_showDataTable")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (model != null) {
                    ModelEventType event = ModelEventType.STEP;
                    if (stateManager.isActive(runModelMicroState))
                        event = ModelEventType.MICROSTEP;
                    showMeasurementDialog(event);
                }
            }
        }.setToolTip(Lang.get("menu_showDataTable_tt")).setEnabledChain(false).setAccelerator("F6");

        showMeasurementGraph = new ToolTipAction(Lang.get("menu_showDataGraph")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (model != null) {
                    ModelEventType event = ModelEventType.STEP;
                    if (stateManager.isActive(runModelMicroState))
                        event = ModelEventType.MICROSTEP;
                    showMeasurementGraph(event);
                }
            }
        }.setToolTip(Lang.get("menu_showDataGraph_tt")).setEnabledChain(false);

        circuitComponent.getInputMap().put(KeyStroke.getKeyStroke(' '), KEY_START_STOP_ACTION);
        circuitComponent.getActionMap().put(KEY_START_STOP_ACTION, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (model == null)
                    runModelAction.actionPerformed(actionEvent);
                else
                    stoppedStateAction.actionPerformed(actionEvent);
            }
        });

        ToolTipAction stats = new ToolTipAction(Lang.get("menu_stats")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    model = new ModelCreator(getCircuitComponent().getCircuit(), library).createModel(false);
                    Statistics stats = new Statistics(model);
                    new StatsDialog(Main.this, stats.getTableModel()).setVisible(true);
                } catch (ElementNotFoundException | PinException | NodeException e) {
                    new ErrorMessage(Lang.get("msg_couldNotCreateStats")).addCause(e).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_stats_tt"));

        JMenu run = new JMenu(Lang.get("menu_sim"));
        menuBar.add(run);
        run.add(showMeasurementDialog.createJMenuItem());
        run.add(showMeasurementGraph.createJMenuItem());
        run.addSeparator();
        run.add(runModelAction.createJMenuItem());
        run.add(runToBreakAction.createJMenuItem());
        run.add(stoppedStateAction.createJMenuItem());
        run.addSeparator();
        run.add(runModelMicroAction.createJMenuItem());
        run.add(doMicroStep.createJMenuItem());
        run.add(runToBreakMicroAction.createJMenuItem());
        run.addSeparator();
        run.add(runTests.createJMenuItem());
        run.add(runAllTests.createJMenuItem());
        run.addSeparator();
        run.add(speedTest.createJMenuItem());
        run.add(stats.createJMenuItem());

        toolBar.add(runModelState.setIndicator(runModelAction.createJButtonNoText()));
        toolBar.add(runToBreakAction.createJButtonNoText());
        toolBar.add(stoppedStateAction.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runModelMicroState.setIndicator(runModelMicroAction.createJButtonNoText()));
        toolBar.add(doMicroStep.createJButtonNoText());
        toolBar.add(runToBreakMicroAction.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runTests.createJButtonNoText());
    }

    /**
     * starts the tests
     */
    public void startTests() {
        try {
            ArrayList<ValueTableDialog.TestSet> tsl = new ArrayList<>();
            for (VisualElement el : circuitComponent.getCircuit().getTestCases())
                tsl.add(new ValueTableDialog.TestSet(
                        el.getElementAttributes().get(TestCaseElement.TESTDATA),
                        el.getElementAttributes().getLabel()));

            if (tsl.isEmpty())
                throw new TestingDataException(Lang.get("err_noTestData"));

            windowPosManager.register("testResult", new ValueTableDialog(Main.this, Lang.get("msg_testResult"))
                    .addTestResult(tsl, circuitComponent.getCircuit(), library))
                    .setVisible(true);

            ensureModelIsStopped();
        } catch (NodeException | ElementNotFoundException | PinException | TestingDataException | RuntimeException e1) {
            showError(Lang.get("msg_runningTestError"), e1);
        }
    }

    /**
     * Creates the analyse menu
     *
     * @param menuBar the menu bar
     */
    private void createAnalyseMenu(JMenuBar menuBar) {
        JMenu analyse = new JMenu(Lang.get("menu_analyse"));
        menuBar.add(analyse);
        analyse.add(new ToolTipAction(Lang.get("menu_analyse")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelCreator(circuitComponent.getCircuit(), new SubstituteLibrary(library)).createModel(false);
                    try {
                        model.checkForInvalidSignals();
                        new TableDialog(Main.this,
                                new ModelAnalyser(model).analyse(),
                                library,
                                getBaseFileName())
                                .setVisible(true);
                        ensureModelIsStopped();
                    } finally {
                        model.close();
                    }
                } catch (PinException | NodeException | AnalyseException | ElementNotFoundException | BacktrackException | RuntimeException e1) {
                    showError(Lang.get("msg_analyseErr"), e1);
                }
            }
        }
                .setToolTip(Lang.get("menu_analyse_tt"))
                .setAccelerator("F9")
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_synthesise")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TruthTable tt = new TruthTable(3).addResult();
                new TableDialog(Main.this, tt, library, getBaseFileName()).setVisible(true);
                ensureModelIsStopped();
            }
        }
                .setToolTip(Lang.get("menu_synthesise_tt"))
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_expression")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ExpressionDialog(Main.this, library, shapeFactory, getBaseFileName()).setVisible(true);
            }
        }
                .setToolTip(Lang.get("menu_expression_tt"))
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_fsm")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                String foundName = null;
                for (VisualElement ve : circuitComponent.getCircuit().getElements()) {
                    if (ve.equalsDescription(Probe.DESCRIPTION)) {
                        String name = ve.getElementAttributes().getLabel();
                        if (name.endsWith(".fsm")) {
                            foundName = name;
                        }
                    }
                }

                new FSMFrame(Main.this, library)
                        .setBaseFileName(filename)
                        .setProbeLabelName(foundName)
                        .setVisible(true);
            }
        }
                .setToolTip(Lang.get("menu_fsm_tt"))
                .createJMenuItem());
    }

    private void orderMeasurements() {
        try {
            Model m = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
            try {
                ensureModelIsStopped();
                ArrayList<String> names = new ArrayList<>();
                for (Signal s : m.getSignals())
                    names.add(s.getName());
                new OrderMerger<String, String>(circuitComponent.getCircuit().getMeasurementOrdering()).order(names);
                ElementOrderer.ListOrder<String> o = new ElementOrderer.ListOrder<>(names);
                if (new ElementOrderer<>(Main.this, Lang.get("menu_orderMeasurements"), o)
                        .addOkButton()
                        .showDialog()) {
                    circuitComponent.modify(new ModifyMeasurementOrdering(names));
                }
            } finally {
                m.close();
            }
        } catch (NodeException | PinException | ElementNotFoundException | RuntimeException e) {
            showError(Lang.get("msg_errorCreatingModel"), e);
        }
    }

    private void setupStates() {
        stoppedState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                clearModelDescription();
                circuitComponent.setModeAndReset(false, SyncAccess.NOSYNC);
                doMicroStep.setEnabled(false);
                stoppedState.getAction().setEnabled(false);
                showMeasurementDialog.setEnabled(false);
                showMeasurementGraph.setEnabled(false);
                runToBreakAction.setEnabled(false);
                runToBreakMicroAction.setEnabled(false);
                runTests.setEnabled(true);
                // keep errors
                if (circuitComponent.getHighLightStyle() != Style.ERROR)
                    circuitComponent.removeHighLighted();
            }

        });
        runModelState = stateManager.register(new RunModelState());
        runModelMicroState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                showMeasurementDialog.setEnabled(true);
                showMeasurementGraph.setEnabled(true);
                stoppedState.getAction().setEnabled(true);
                runTests.setEnabled(false);
                createAndStartModel(false, ModelEventType.MICROSTEP, null);
            }
        });
        stateManager.setActualState(stoppedState);
    }

    /**
     * @return returns true if one of the children has the focus.
     */
    public boolean hasMouseFocus() {
        return checkFocus(getContentPane());
    }

    private static boolean checkFocus(Container contentPane) {
        for (int i = 0; i < contentPane.getComponentCount(); i++) {
            Component c = contentPane.getComponent(i);
            if (c.hasFocus())
                return true;
            if (c instanceof Container)
                if (checkFocus((Container) c))
                    return true;
        }
        return false;
    }

    private class RunModelState extends State {
        @Override
        public void enter() {
            enter(true, null);
        }

        void enter(boolean runRealTime, ModelModifier modelModifier) {
            super.enter();
            stoppedState.getAction().setEnabled(true);
            showMeasurementDialog.setEnabled(true);
            showMeasurementGraph.setEnabled(true);
            runTests.setEnabled(false);
            createAndStartModel(runRealTime, ModelEventType.STEP, modelModifier);
        }
    }

    private void clearModelDescription() {
        if (model != null)
            model.modify(() -> model.close());

        modelCreator = null;
        model = null;
    }

    private void createAndStartModel(boolean globalRunClock, ModelEventType updateEvent, ModelModifier modelModifier) {
        try {
            circuitComponent.removeHighLighted();

            long time = System.currentTimeMillis();

            modelCreator = new ModelCreator(circuitComponent.getCircuit(), library);

            if (model != null) {
                model.modify(() -> {
                    ModelClosedObserver mco = model.getObserver(ModelClosedObserver.class);
                    if (mco != null) mco.setClosedByRestart(true);
                    model.close();
                });
                circuitComponent.getCircuit().clearState();
                model = null;
            }

            model = modelCreator.createModel(true);

            time = System.currentTimeMillis() - time;
            LOGGER.debug("model creation: " + time + " ms");

            model.setWindowPosManager(windowPosManager);

            statusLabel.setText(Lang.get("msg_N_nodes", model.size()));

            int maxFrequency = 0;
            realTimeClockRunning = false;
            if (globalRunClock) {
                int threadRunnerCount = 0;
                for (Clock c : model.getClocks()) {
                    int frequency = c.getFrequency();
                    if (frequency > 0) {
                        final RealTimeClock realTimeClock = new RealTimeClock(model, c, timerExecutor, this);
                        model.addObserver(realTimeClock);
                        if (realTimeClock.isThreadRunner()) threadRunnerCount++;
                        realTimeClockRunning = true;
                    }
                    if (frequency > maxFrequency)
                        maxFrequency = frequency;
                }
                if (threadRunnerCount > 1)
                    throw new RuntimeException(Lang.get("err_moreThanOneFastClock"));
            }
            if (!realTimeClockRunning && updateEvent == ModelEventType.MICROSTEP) {
                // no real clock
                AsyncSeq ai = model.getAsyncInfos();
                if (ai != null) {
                    if (ai.getFrequency() > 0) {
                        if (!model.getClocks().isEmpty())
                            throw new RuntimeException(Lang.get("err_clocksNotAllowedInAsyncMode"));
                        model.addObserver(
                                new AsyncSequentialClock(model, ai, timerExecutor));
                        realTimeClockRunning = true;
                    }
                    model.setAsyncMode();
                }
            }

            circuitComponent.setModeAndReset(true, model);

            modelCreator.connectToGui();

            handleKeyboardComponents();

            doMicroStep.setEnabled(false);
            if (!realTimeClockRunning && model.isRunToBreakAllowed()) {
                if (updateEvent == ModelEventType.MICROSTEP)
                    runToBreakMicroAction.setEnabled(true);
                else
                    runToBreakAction.setEnabled(true);
            }

            ElementAttributes settings = circuitComponent.getCircuit().getAttributes();
            if (settings.get(Keys.SHOW_DATA_TABLE) || windowPosManager.isVisible("probe"))
                showMeasurementDialog(updateEvent);

            if (settings.get(Keys.SHOW_DATA_GRAPH) || windowPosManager.isVisible("dataSet"))
                showMeasurementGraph(updateEvent);
            if (settings.get(Keys.SHOW_DATA_GRAPH_MICRO))
                showMeasurementGraph(ModelEventType.MICROSTEP);

            if (modelModifier != null)
                modelModifier.preInit(model);
            else {
                if (settings.get(Keys.PRELOAD_PROGRAM))
                    new ProgramMemoryLoader(settings.get(Keys.PROGRAM_TO_PRELOAD)).preInit(model);
            }

            if (updateEvent == ModelEventType.MICROSTEP) {
                checkMicroStepActions(model);
                model.addObserver(new MicroStepObserver(model));
            } else if (updateEvent == ModelEventType.STEP) {
                if (maxFrequency <= 50)
                    model.addObserver(new FullStepObserver(model));
                else
                    model.addObserver(new FastObserver());
            }

            model.addObserver(new ModelClosedObserver());

            model.init();

        } catch (NodeException | PinException | RuntimeException | ElementNotFoundException e) {
            if (model != null)
                model.close();
            showError(Lang.get("msg_errorCreatingModel"), e);
        }
    }

    private void checkMicroStepActions(Model model) {
        final boolean needsUpdate = model.needsUpdate();
        doMicroStep.setEnabled(needsUpdate);
        if (!model.isRunToBreakAllowed())
            runToBreakMicroAction.setEnabled(needsUpdate);
    }

    private void handleKeyboardComponents() {
        for (Keyboard k : model.findNode(Keyboard.class))
            windowPosManager.register("keyboard_" + k.getLabel(), new KeyboardDialog(this, k, model));
    }

    private void showMeasurementGraph(ModelEventType updateEvent) {
        List<String> ordering = circuitComponent.getCircuit().getMeasurementOrdering();
        windowPosManager.register("dataSet", GraphDialog.createLiveDialog(this, model, updateEvent == ModelEventType.MICROSTEP, ordering)).setVisible(true);
    }

    private void showMeasurementDialog(ModelEventType updateEvent) {
        List<String> ordering = circuitComponent.getCircuit().getMeasurementOrdering();
        windowPosManager.register("probe", new ProbeDialog(this, model, updateEvent, ordering)).setVisible(true);
    }

    /**
     * @return the model or null if no model is running
     */
    public Model getModel() {
        return model;
    }

    private void showError(String message, Exception cause) {
        if (cause instanceof NodeException) {
            NodeException e = (NodeException) cause;
            circuitComponent.addHighLightedWires(e.getValues());
            circuitComponent.addHighLighted(e.getVisualElement());
            if (modelCreator != null)
                modelCreator.addNodeElementsTo(e.getNodes(), circuitComponent.getHighLighted());
        } else if (cause instanceof PinException) {
            PinException e = (PinException) cause;
            circuitComponent.addHighLighted(e.getVisualElement());
            if (e.getNet() != null)
                circuitComponent.addHighLighted(e.getNet().getWires());
        } else if (cause instanceof BurnException) {
            BurnException e = (BurnException) cause;
            circuitComponent.addHighLightedWires(e.getValues());
        }
        circuitComponent.setHighLightStyle(Style.ERROR);
        circuitComponent.graphicHasChanged();
        new ErrorMessage(message).addCause(cause).show(Main.this);
        ensureModelIsStopped();
    }

    /**
     * stops the model
     */
    public void ensureModelIsStopped() {
        if (!stoppedState.isActive())
            stoppedState.enter();
    }


    private static JFileChooser getJFileChooser(File filename) {
        File folder = null;
        if (filename != null)
            folder = filename.getParentFile();

        JFileChooser fileChooser = new MyFileChooser(folder);
        fileChooser.setFileFilter(new FileNameExtensionFilter("Circuit", "dig"));
        return fileChooser;
    }

    @Override
    public boolean isStateChanged() {
        return circuitComponent.isModified();
    }

    @Override
    public void saveChanges() {
        save.actionPerformed(null);
    }

    @Override
    public void open(File file, boolean newWindow) {
        if (newWindow) {
            new MainBuilder()
                    .setParent(Main.this)
                    .setFileToOpen(file)
                    .build()
                    .setVisible(true);
        } else {
            if (ClosingWindowListener.checkForSave(Main.this, Main.this))
                loadFile(file, true, true);
        }
    }

    private void loadFile(File filename, boolean setLibraryRoot, boolean toPref) {
        try {
            if (setLibraryRoot) {
                library.setRootFilePath(filename.getParentFile());
                if (library.getWarningMessage() != null)
                    SwingUtilities.invokeLater(new ErrorMessage(library.getWarningMessage().toString()).setComponent(this));
            }
            Circuit circuit = Circuit.loadCircuit(filename, shapeFactory);
            circuitComponent.setCircuit(circuit);

            // requests the circuit modified state, so place it behind circuitComponent.setCircuit(circuit);
            setFilename(filename, toPref);

            ensureModelIsStopped();
            windowPosManager.closeAll();
            statusLabel.setText(" ");
        } catch (Exception e) {
            circuitComponent.setCircuit(new Circuit());
            setFilename(null, false);
            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File filename, boolean toPrefs) {
        try {
            circuitComponent.save(filename);
            ensureModelIsStopped();
            setFilename(filename, toPrefs);

            library.invalidateElement(filename);

            if (library.getRootFilePath() == null) {
                library.setRootFilePath(filename.getParentFile());
                if (library.getWarningMessage() != null)
                    SwingUtilities.invokeLater(new ErrorMessage(library.getWarningMessage().toString()).setComponent(this));
            }
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e).show(this);
        }
    }

    private void setFilename(File filename, boolean toPrefs) {
        modifiedPrefixVisible = circuitComponent.isModified();
        if (save != null)
            save.setEnabled(modifiedPrefixVisible);
        String prefix = "";
        if (modifiedPrefixVisible)
            prefix = "*";
        this.filename = filename;
        if (filename != null) {
            this.baseFilename = filename;
            if (toPrefs)
                fileHistory.add(filename);
            setTitle(prefix + filename + " - " + Lang.get("digital"));
        } else {
            setTitle(prefix + Lang.get("digital"));
        }
    }


    @Override
    public void hasChanged() {
        ensureModelIsStopped();
        if (modifiedPrefixVisible != circuitComponent.isModified())
            setFilename(filename, false);
    }

    /**
     * @return the window position manager
     */
    public WindowPosManager getWindowPosManager() {
        return windowPosManager;
    }

    @Override
    public void setStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    /**
     * Used to update the gui if the model is closed
     */
    private class ModelClosedObserver implements ModelStateObserverTyped {

        private boolean closedByRestart = false;

        @Override
        public void handleEvent(ModelEvent event) {
            switch (event.getType()) {
                case ERROR_OCCURRED:
                    SwingUtilities.invokeLater(() -> showError(Lang.get("msg_errorCalculatingStep"), event.getCause()));
                    break;
                case CLOSED:
                    if (!closedByRestart)
                        SwingUtilities.invokeLater(Main.this::ensureModelIsStopped);
                    break;
            }
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.CLOSED, ModelEventType.ERROR_OCCURRED};
        }

        public void setClosedByRestart(boolean closedByRestart) {
            this.closedByRestart = closedByRestart;
        }
    }

    /**
     * Updates the graphic at every modification.
     */
    private class FullStepObserver implements ModelStateObserverTyped {
        private final Model model;

        FullStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void handleEvent(ModelEvent event) {
            switch (event.getType()) {
                case EXTERNALCHANGE:
                    model.doStep();
                    circuitComponent.graphicHasChanged();
                    break;
                case BREAK:
                    circuitComponent.graphicHasChanged();
                    break;
            }
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.EXTERNALCHANGE, ModelEventType.BREAK};
        }
    }

    /**
     * Updates the graphic at every 100ms
     */
    private class FastObserver implements ModelStateObserverTyped {
        private final Timer timer;

        FastObserver() {
            timer = new Timer(100, actionEvent -> circuitComponent.graphicHasChanged());
        }

        @Override
        public void handleEvent(ModelEvent event) {
            switch (event.getType()) {
                case STARTED:
                    timer.start();
                    break;
                case CLOSED:
                case BREAK:
                    timer.stop();
                    SwingUtilities.invokeLater(circuitComponent::graphicHasChanged);
                    break;
            }
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.CLOSED, ModelEventType.BREAK};
        }
    }

    /**
     * Updates the graphic at every micro step
     */
    private class MicroStepObserver implements ModelStateObserverTyped {
        private final Model model;

        MicroStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void handleEvent(ModelEvent event) {
            switch (event.getType()) {
                case EXTERNALCHANGE:
                case MICROSTEP:
                case BREAK:
                    if (!realTimeClockRunning) {
                        circuitComponent.removeHighLighted();
                        modelCreator.addNodeElementsTo(model.nodesToUpdate(), circuitComponent.getHighLighted());
                    }
                    circuitComponent.graphicHasChanged();
                    if (!realTimeClockRunning)
                        checkMicroStepActions(model);
                    break;
            }
        }

        @Override
        public ModelEventType[] getEvents() {
            return new ModelEventType[]{ModelEventType.EXTERNALCHANGE, ModelEventType.MICROSTEP, ModelEventType.BREAK};
        }
    }

    private class ExportAction extends ToolTipAction {
        private final String name;
        private final String suffix;
        private final ExportFactory exportFactory;

        ExportAction(String name, String suffix, ExportFactory exportFactory) {
            super(name);
            this.name = name;
            this.suffix = suffix;
            this.exportFactory = exportFactory;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new MyFileChooser();
            if (filename != null)
                fc.setSelectedFile(SaveAsHelper.checkSuffix(filename, suffix));

            ElementAttributes settings = Settings.getInstance().getAttributes();
            File exportDir = settings.getFile("exportDirectory");
            if (exportDir != null)
                fc.setCurrentDirectory(exportDir);

            fc.addChoosableFileFilter(new FileNameExtensionFilter(name, suffix));
            new SaveAsHelper(Main.this, fc, suffix).checkOverwrite(
                    file -> {
                        settings.setFile("exportDirectory", file.getParentFile());
                        try (OutputStream out = new FileOutputStream(file)) {
                            new Export(circuitComponent.getCircuit(), exportFactory).export(out);
                        }
                    }
            );
        }
    }

    private class ExportGifAction extends ToolTipAction {
        private final String name;

        ExportGifAction(String name) {
            super(name);
            this.name = name;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new MyFileChooser();
            if (filename != null)
                fc.setSelectedFile(SaveAsHelper.checkSuffix(filename, "gif"));

            ElementAttributes settings = Settings.getInstance().getAttributes();
            File exportDir = settings.getFile("exportDirectory");
            if (exportDir != null)
                fc.setCurrentDirectory(exportDir);

            fc.addChoosableFileFilter(new FileNameExtensionFilter(name, "gif"));
            new SaveAsHelper(Main.this, fc, "gif").checkOverwrite(
                    file -> {
                        settings.setFile("exportDirectory", file.getParentFile());
                        GifExporter gifExporter = new GifExporter(Main.this, circuitComponent.getCircuit(), 500, file);
                        windowPosManager.closeAll();
                        runModelState.enter(false, gifExporter);
                        circuitComponent.graphicHasChanged();
                    }
            );
        }
    }

    //***********************
    // remote interface start
    //***********************

    private static class AddressPicker {
        private long address;

        private void getProgramROMAddress(Model model) {
            List<Node> programCounters = model.findNode(n -> n instanceof ProgramCounter && ((ProgramCounter) n).isProgramCounter());
            if (programCounters.size() == 1)
                address = ((ProgramCounter) programCounters.get(0)).getProgramCounter();
            else
                address = -1;
        }

        String getAddressString() {
            if (address < 0)
                return null;
            else
                return Long.toHexString(address);
        }
    }

    @Override
    public void start(File romHex) {
        SwingUtilities.invokeLater(() -> {
            ProgramMemoryLoader modelModifier = null;
            if (romHex != null)
                modelModifier = new ProgramMemoryLoader(romHex);
            runModelState.enter(true, modelModifier);
            circuitComponent.graphicHasChanged();
        });
    }

    @Override
    public void debug(File romHex) {
        SwingUtilities.invokeLater(() -> {
            runModelState.enter(false, new ProgramMemoryLoader(romHex));
            circuitComponent.graphicHasChanged();
            if (model != null)
                showMeasurementDialog(ModelEventType.STEP);
        });
    }

    @Override
    public String doSingleStep() throws RemoteException {
        if (model != null && !realTimeClockRunning) {
            try {
                AddressPicker addressPicker = new AddressPicker();
                SwingUtilities.invokeAndWait(() -> {
                    ArrayList<Clock> cl = model.getClocks();
                    if (cl.size() == 1) {
                        ObservableValue clkVal = cl.get(0).getClockOutput();
                        model.modify(() -> clkVal.setBool(!clkVal.getBool()));
                        model.doStep();
                        if (model != null) {
                            if (clkVal.getBool() && model.isRunning()) {
                                model.modify(() -> clkVal.setBool(!clkVal.getBool()));
                                model.doStep();
                            }
                            addressPicker.getProgramROMAddress(model);
                        }
                    }
                });
                return addressPicker.getAddressString();
            } catch (InterruptedException | InvocationTargetException e) {
                throw new RemoteException("error performing a single step " + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public String runToBreak() throws RemoteException {
        try {
            AddressPicker addressPicker = new AddressPicker();
            SwingUtilities.invokeAndWait(() -> {
                if (model != null && model.isRunToBreakAllowed() && !realTimeClockRunning) {
                    runToBreakAction.actionPerformed(null);
                    addressPicker.getProgramROMAddress(model);
                }
            });
            return addressPicker.getAddressString();
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RemoteException("error performing a run to break " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        SwingUtilities.invokeLater(this::ensureModelIsStopped);
    }

    @Override
    public String measure() throws RemoteException {
        if (model == null)
            throw new RemoteException("no model available");

        StringBuilder sb = new StringBuilder("{");
        model.read(() -> {
            boolean first = true;
            for (Signal s : model.getSignals()) {
                if (first) first = false;
                else sb.append(',');
                sb.append('"').append(s.getName()).append("\":").append(s.getValue().getValue());
            }
        });
        sb.append("}");
        return sb.toString();
    }

    //**********************
    // remote interface end
    //**********************

    /**
     * Starts the main app
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new DigitalUncaughtExceptionHandler());

        /*
        The Apple look an feel, which can be enabled by choosing the UIManager.getSystemLookAndFeelClassName()
        on MacOS has problems with the component tree view because it does not support different item heights.
        Also, the HTML rendering does not seem to be supported. See GitHub #190.
        Therefore also on MosOS the MetalLookAndFeel is used.
         */
        try { // enforce MetalLookAndFeel
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        URL.setURLStreamHandlerFactory(ElementHelpDialog.createURLStreamHandlerFactory());
        FormatToExpression.setDefaultFormat(Settings.getInstance().get(Keys.SETTINGS_EXPRESSION_FORMAT));

        if (Screen.isMac()) {
            setMacCopyPasteTo(UIManager.get("TextField.focusInputMap"));
            setMacCopyPasteTo(UIManager.get("TextArea.focusInputMap"));
        }

        File file = null;
        for (String s : args) {
            if (s.equals("experimental")) experimental = true;
            else if (s.trim().length() > 0) {
                File f = new File(s);
                if (f.exists())
                    file = f;
            }
        }

        if (file != null && file.getName().endsWith(".fsm")) {
            FSMFrame.openFile(file);
        } else if (file != null && file.getName().endsWith(".tru")) {
            TableDialog.openFile(file);
        } else {
            MainBuilder builder = new MainBuilder().setMainFrame();
            if (file != null)
                builder.setFileToOpen(file);
            SwingUtilities.invokeLater(() -> {
                final boolean tutorial = Settings.getInstance().getAttributes().get(Keys.SETTINGS_SHOW_TUTORIAL);
                if (tutorial)
                    builder.setCircuit(new Circuit());

                Main main = builder.build();
                try {
                    new RemoteSever(new DigitalHandler(main)).start(41114);
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> main.statusLabel.setText(Lang.get("err_portIsInUse")));
                }
                main.setVisible(true);

                if (tutorial)
                    new InitialTutorial(main).setVisible(true);

                CheckForNewRelease.showReleaseDialog(main);
            });
        }
    }

    private static void setMacCopyPasteTo(Object obj) {
        if (obj instanceof InputMap) {
            InputMap im = (InputMap) obj;
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }
    }

    /**
     * Builder to create a Main-Window
     */
    public static class MainBuilder {
        private File fileToOpen;
        private Window parent;
        private ElementLibrary library;
        private Circuit circuit;
        private boolean allowAllFileActions = true;
        private File baseFileName;
        private boolean keepPrefMainFile;
        private boolean mainFrame = false;

        /**
         * @param fileToOpen the file to open
         * @return this for chained calls
         */
        public MainBuilder setFileToOpen(File fileToOpen) {
            this.fileToOpen = fileToOpen;
            this.baseFileName = fileToOpen;
            return this;
        }

        /**
         * @param baseFileName filename used as base for save and load operations
         * @return this for chained calls
         */
        public MainBuilder setBaseFileName(File baseFileName) {
            this.baseFileName = baseFileName;
            return this;
        }

        /**
         * @param parent the parent component
         * @return this for chained calls
         */
        public MainBuilder setParent(Window parent) {
            this.parent = parent;
            return this;
        }

        /**
         * @param library the library to use
         * @return this for chained calls
         */
        public MainBuilder setLibrary(ElementLibrary library) {
            this.library = library;
            return this;
        }

        /**
         * @param circuit the circuit to show
         * @return this for chained calls
         */
        public MainBuilder setCircuit(Circuit circuit) {
            this.circuit = circuit;
            return this;
        }

        /**
         * If called, most file actions are denied
         *
         * @return this for chained calls
         */
        public MainBuilder denyMostFileActions() {
            this.allowAllFileActions = false;
            return this;
        }

        /**
         * Keeps the main file defined in the preferences
         *
         * @return this for chained calls
         */
        public MainBuilder keepPrefMainFile() {
            this.keepPrefMainFile = true;
            return this;
        }

        /**
         * Creates a new Main instance
         *
         * @return a new Main instance
         */
        public Main build() {
            return new Main(this);
        }

        /**
         * Opens the frame using SwingUtilities.invokeLater
         */
        public void openLater() {
            SwingUtilities.invokeLater(() -> build().setVisible(true));
        }

        private MainBuilder setMainFrame() {
            mainFrame = true;
            return this;
        }
    }

    private class ModelKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            checkKey(keyEvent.getKeyCode(), true);
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            checkKey(keyEvent.getKeyCode(), false);
        }

        private void checkKey(int keyCode, boolean pressed) {
            if (model != null && keyCode != KeyEvent.VK_UNDEFINED) {
                Button b = model.getButtonToMap(keyCode);
                if (b != null) {
                    model.modify(() -> b.setPressed(pressed));
                }
            }
        }
    }
}
