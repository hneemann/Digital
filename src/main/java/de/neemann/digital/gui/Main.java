package de.neemann.digital.gui;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.io.PowerSupply;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.gif.GifExporter;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.*;
import de.neemann.digital.gui.components.data.GraphDialog;
import de.neemann.digital.gui.components.expression.ExpressionDialog;
import de.neemann.digital.gui.components.modification.Modifications;
import de.neemann.digital.gui.components.modification.ModifyAttribute;
import de.neemann.digital.gui.components.modification.ModifyMeasurementOrdering;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.components.testing.ValueTableDialog;
import de.neemann.digital.gui.components.tree.LibraryTreeModel;
import de.neemann.digital.gui.components.tree.SelectTree;
import de.neemann.digital.gui.remote.DigitalHandler;
import de.neemann.digital.gui.remote.RemoteException;
import de.neemann.digital.gui.remote.RemoteSever;
import de.neemann.digital.gui.state.State;
import de.neemann.digital.gui.state.StateManager;
import de.neemann.digital.gui.sync.LockSync;
import de.neemann.digital.gui.sync.NoSync;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.gui.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static javax.swing.JOptionPane.showInputDialog;

/**
 * The main frame of the Digital Simulator
 * Set log level: -Dorg.slf4j.simpleLogger.defaultLogLevel=debug
 *
 * @author hneemann
 */
public final class Main extends JFrame implements ClosingWindowListener.ConfirmSave, ErrorStopper, FileHistory.OpenInterface, DigitalRemoteInterface, StatusInterface, Circuit.ChangedListener {
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();
    private static final String KEY_START_STOP_ACTION = "startStop";
    private static boolean experimental;

    /**
     * @return true if experimental features are enabled
     */
    public static boolean enableExperimental() {
        return experimental;
    }

    static {
        ATTR_LIST.add(Keys.SHOW_DATA_TABLE);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH_MICRO);
    }

    private static final String MESSAGE = Lang.get("message");
    private static final Icon ICON_RUN = IconCreator.create("media-playback-start.png");
    private static final Icon ICON_MICRO = IconCreator.create("media-playback-start-2.png");
    private static final Icon ICON_TEST = IconCreator.create("media-playback-start-T.png");
    private static final Icon ICON_STEP = IconCreator.create("media-seek-forward.png");
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
    private final ToolTipAction save;
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final JLabel statusLabel;
    private final StateManager stateManager = new StateManager();
    private final ScheduledThreadPoolExecutor timerExecutor = new ScheduledThreadPoolExecutor(1);
    private final WindowPosManager windowPosManager = new WindowPosManager();
    private final InsertHistory insertHistory;
    private final boolean keepPrefMainFile;

    private ToolTipAction doStep;
    private ToolTipAction runToBreakAction;

    private File baseFilename;
    private File filename;
    private FileHistory fileHistory;
    private boolean modifiedPrefixVisible = false;

    private Sync modelSync;
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

        keepPrefMainFile = builder.keepPrefMainFile;

        if (builder.library != null) library = builder.library;
        else library = new ElementLibrary();

        shapeFactory = new ShapeFactory(library, Settings.getInstance().get(Keys.SETTINGS_IEEE_SHAPES));

        fileHistory = new FileHistory(this);

        baseFilename = builder.baseFileName;

        circuitComponent = new CircuitComponent(this, library, shapeFactory);
        circuitComponent.getCircuit().addListener(this);
        if (builder.circuit != null) {
            SwingUtilities.invokeLater(() -> circuitComponent.setCircuit(builder.circuit));
            setFilename(builder.fileToOpen, false);
        } else {
            if (builder.fileToOpen != null) {
                SwingUtilities.invokeLater(() -> loadFile(builder.fileToOpen, builder.library == null, false));
            } else {
                File name = fileHistory.getMostRecent();
                if (name != null) {
                    SwingUtilities.invokeLater(() -> loadFile(name, true, false));
                }
            }
        }

        library.addListener(circuitComponent);

        getContentPane().add(circuitComponent);
        componentOnPane = circuitComponent;

        statusLabel = new JLabel(" ");
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        setupStates();

        JMenuBar menuBar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        save = createFileMenu(menuBar, toolBar, builder.allowAllFileActions);
        toolBar.addSeparator();

        createViewMenu(menuBar, toolBar);

        toolBar.addSeparator();

        createEditMenu(menuBar);

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
                windowPosManager.closeAll();
            }
        });

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(menuBar);
        JMenu help = InfoDialog.getInstance().addToFrame(this, MESSAGE+"\n\nlib: "+ElementLibrary.getLibPath());
        help.add(new ToolTipAction(Lang.get("menu_help_elements")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    new ElementHelpDialog(Main.this, library, shapeFactory).setVisible(true);
                } catch (NodeException | PinException e) {
                    new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_help_elements_tt")).createJMenuItem());

        new ToolTipAction("insertLast") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                InsertAction lastInsertAction = insertHistory.getLastInsertAction();
                if (lastInsertAction != null)
                    lastInsertAction.actionPerformed(actionEvent);
            }
        }.setAccelerator("L").enableAcceleratorIn(circuitComponent);

        enableClockShortcut();

        setPreferredSize(Screen.getInstance().scale(new Dimension(1024, 768)));
        pack();
        setLocationRelativeTo(builder.parent);
    }

    private void enableClockShortcut() {
        new ToolTipAction("doClock") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (model != null && !realTimeClockRunning) {
                    ArrayList<Clock> cl = model.getClocks();
                    if (cl.size() == 1) {
                        ObservableValue clkVal = cl.get(0).getClockOutput();
                        clkVal.setBool(!clkVal.getBool());
                        try {
                            model.doStep();
                            circuitComponent.repaintNeeded();
                        } catch (NodeException | RuntimeException e) {
                            showErrorAndStopModel(Lang.get("err_remoteExecution"), e);
                        }
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
                circuitComponent.scaleCircuit(1.25);
            }
        }.setAccelerator("control PLUS");
        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOM_OUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.scaleCircuit(0.8);
            }
        }.setAccelerator("control MINUS");

        ToolTipAction viewHelp = new ToolTipAction(Lang.get("menu_viewHelp"), ICON_HELP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Circuit circuit = circuitComponent.getCircuit();
                final String name = Lang.get("msg_actualCircuit");
                File file = filename;
                if (file == null)
                    file = new File(name);
                try {
                    ElementLibrary.ElementTypeDescriptionCustom description =
                            new ElementLibrary.ElementTypeDescriptionCustom(file,
                                    attributes -> new CustomElement(circuit, library, filename),
                                    circuit.getAttributes(), circuit.getInputNames());
                    description.setShortName(name);
                    description.setDescription(circuit.getAttributes().get(Keys.DESCRIPTION));
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
                split.setRightComponent(circuitComponent);
                getContentPane().add(split);
                componentOnPane = split;
            } else {
                if (treeModel != null) {
                    library.removeListener(treeModel);
                    treeModel = null;
                }
                getContentPane().add(circuitComponent);
                componentOnPane = circuitComponent;
            }
            revalidate();
        });
        treeCheckBox.setAccelerator(KeyStroke.getKeyStroke("F5"));

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
        view.add(viewHelp.createJMenuItem());
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
                    circuitComponent.setCircuit(new Circuit());
                    setFilename(null, true);
                    windowPosManager.closeAll();
                    try {
                        library.setRootFilePath(null);
                    } catch (IOException e1) {
                        // can not happen, no folder is scanned
                    }
                }
            }
        }.setToolTip(Lang.get("menu_new_tt")).setActive(allowAll);

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
        }.setActive(allowAll);

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
        }.setToolTip(Lang.get("menu_openWin_tt")).setActive(allowAll);

        JMenu openRecent = new JMenu(Lang.get("menu_openRecent"));
        fileHistory.setMenu(openRecent);
        openRecent.setEnabled(allowAll);

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
                                        break;
                                    case 1:
                                        saveAsHelper.retryFileSelect();
                                }
                            }
                        }
                );
            }
        }.setActive(allowAll);

        ToolTipAction save = new ToolTipAction(Lang.get("menu_save"), ICON_SAVE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveAs.actionPerformed(e);
                else
                    saveFile(filename, false);
            }
        }.setAcceleratorCTRLplus('S').setActive(!experimental);

        JMenu export = new JMenu(Lang.get("menu_export"));
        export.add(new ExportAction(Lang.get("menu_exportSVG"), "svg", GraphicSVGIndex::new));
        export.add(new ExportAction(Lang.get("menu_exportSVGLaTex"), "svg", GraphicSVGLaTeX::new));
        export.add(new ExportAction(Lang.get("menu_exportPNGSmall"), "png", (out) -> new GraphicsImage(out, "PNG", 1)));
        export.add(new ExportAction(Lang.get("menu_exportPNGLarge"), "png", (out) -> new GraphicsImage(out, "PNG", 2)));
        if (enableExperimental())
            export.add(new ExportGifAction(Lang.get("menu_exportAnimatedGIF")));

        JMenu file = new JMenu(Lang.get("menu_file"));
        menuBar.add(file);
        file.add(newFile.createJMenuItem());
        file.add(newSubFile.createJMenuItem());
        file.add(openRecent);
        file.add(open.createJMenuItem());
        file.add(openWin.createJMenuItem());
        file.add(save.createJMenuItem());
        file.add(saveAs.createJMenuItem());
        file.add(export);

        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());

        return save;
    }

    private File getBaseFileName() {
        if (filename != null) return filename;
        return baseFilename;
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
                circuitComponent.editCircuitAttributes(Main.this);
            }
        }.setToolTip(Lang.get("menu_editAttributes_tt"));

        ToolTipAction editSettings = new ToolTipAction(Lang.get("menu_editSettings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementAttributes modified = new AttributeDialog(Main.this, Settings.getInstance().getKeys(), Settings.getInstance().getAttributes()).showDialog();
                if (modified != null) {
                    FormatToExpression.setDefaultFormat(modified.get(Keys.SETTINGS_EXPRESSION_FORMAT));
                    if (!Settings.getInstance().getAttributes().equalsKey(Keys.SETTINGS_LANGUAGE, modified)
                            || !Settings.getInstance().getAttributes().equalsKey(Keys.SETTINGS_IEEE_SHAPES, modified)
                            || !Settings.getInstance().getAttributes().equalsKey(Keys.SETTINGS_FONT_SCALING, modified)) {
                        Lang.setLanguage(modified.get(Keys.SETTINGS_LANGUAGE));
                        JOptionPane.showMessageDialog(Main.this, Lang.get("msg_restartNeeded"));
                    }
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
                        ArrayList<Movable> elements = CircuitTransferable.createList(data, shapeFactory, new Vector(0, 0));
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
                } catch (Exception e1) {
                    e1.printStackTrace();
                    SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_clipboardContainsNoImportableData")).setComponent(Main.this));
                }
            }
        }.setToolTip(Lang.get("menu_insertAsNew_tt"));

        edit.add(circuitComponent.getUndoAction().createJMenuItemNoIcon());
        edit.add(circuitComponent.getRedoAction().createJMenuItemNoIcon());
        edit.addSeparator();
        edit.add(editAttributes.createJMenuItem());
        edit.add(actualToDefault.createJMenuItem());
        edit.add(restoreAllFuses.createJMenuItem());
        edit.add(createSpecialEditMenu());
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
                        Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_addPrefix"));
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
                    Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_removePrefix"));
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
                    Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_removePinNumbers"));
                    for (VisualElement v : circuitComponent.getCircuit().getElements()) {
                        if (v.equalsDescription(In.DESCRIPTION)
                                || v.equalsDescription(Clock.DESCRIPTION)
                                || v.equalsDescription(Out.DESCRIPTION)) {
                            ElementAttributes attr = v.getElementAttributes();
                            int p = attr.get(Keys.PINNUMBER);
                            if (p > 0)
                                builder.add(new ModifyAttribute<>(v, Keys.PINNUMBER, 0));
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
                    for (VisualElement v : circuitComponent.getCircuit().getElements()) {
                        if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Out.DESCRIPTION))
                            maxNum = Math.max(maxNum, v.getElementAttributes().get(Keys.PINNUMBER));
                    }
                    if ((maxNum & 1) != 0) maxNum++;

                    // defines the power supply circuit
                    ArrayList<Movable> list = new ArrayList<>();
                    list.add(new VisualElement(PowerSupply.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setPos(new Vector(SIZE * 2, 0)));
                    list.add(new VisualElement(In.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setAttribute(Keys.LABEL, "VDD")
                            .setAttribute(Keys.PINNUMBER, maxNum)
                            .setPos(new Vector(0, 0)));
                    list.add(new VisualElement(In.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setAttribute(Keys.LABEL, "GND")
                            .setAttribute(Keys.PINNUMBER, maxNum / 2)
                            .setPos(new Vector(0, SIZE * 2)));
                    list.add(new Wire(new Vector(0, 0), new Vector(SIZE * 2, 0)));
                    list.add(new Wire(new Vector(0, SIZE * 2), new Vector(SIZE, SIZE * 2)));
                    list.add(new Wire(new Vector(SIZE, SIZE * 2), new Vector(SIZE, SIZE)));
                    list.add(new Wire(new Vector(SIZE, SIZE), new Vector(SIZE * 2, SIZE)));
                    circuitComponent.setPartsToInsert(list);
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
        doStep = new ToolTipAction(Lang.get("menu_step"), ICON_STEP) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    model.doMicroStep(false);
                    circuitComponent.removeHighLighted();
                    modelCreator.addNodeElementsTo(model.nodesToUpdate(), circuitComponent.getHighLighted());
                    circuitComponent.repaintNeeded();
                    doStep.setEnabled(model.needsUpdate());
                } catch (Exception e1) {
                    SwingUtilities.invokeLater(
                            new ErrorMessage(Lang.get("msg_errorCalculatingStep")).addCause(e1).setComponent(Main.this)
                    );
                }
            }
        }.setToolTip(Lang.get("menu_step_tt"));

        ToolTipAction runModelAction = runModelState.createToolTipAction(Lang.get("menu_run"), ICON_RUN)
                .setToolTip(Lang.get("menu_run_tt"));
        ToolTipAction runModelMicroAction = runModelMicroState.createToolTipAction(Lang.get("menu_micro"), ICON_MICRO)
                .setToolTip(Lang.get("menu_micro_tt"));
        runToBreakAction = new ToolTipAction(Lang.get("menu_fast"), ICON_FAST) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int i = model.runToBreak();
                    circuitComponent.repaintNeeded();
                    statusLabel.setText(Lang.get("stat_clocks", i));
                } catch (NodeException e1) {
                    ensureModelIsStopped();
                    new ErrorMessage(Lang.get("msg_fastRunError")).addCause(e1).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_fast_tt")).setActive(false);

        ToolTipAction stoppedStateAction = stoppedState.createToolTipAction(Lang.get("menu_element"), ICON_STOP).setToolTip(Lang.get("menu_element_tt"));

        ToolTipAction runTests = new ToolTipAction(Lang.get("menu_runTests"), ICON_TEST) {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTests();
            }
        }.setToolTip(Lang.get("menu_runTests_tt"));

        ToolTipAction speedTest = new ToolTipAction(Lang.get("menu_speedTest")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);

                    SpeedTest speedTest = new SpeedTest(model);
                    String frequency = Integer.toString(speedTest.calculate() / 1000);
                    circuitComponent.getCircuit().clearState();
                    JOptionPane.showMessageDialog(Main.this, Lang.get("msg_frequency_N", frequency));
                } catch (Exception e1) {
                    new ErrorMessage(Lang.get("msg_speedTestError")).addCause(e1).show();
                }
            }
        }.setToolTip(Lang.get("menu_speedTest_tt"));

        ToolTipAction editRunAttributes = new ToolTipAction(Lang.get("menu_editRunAttributes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.editCircuitAttributes(Main.this, ATTR_LIST);
            }
        }.setToolTip(Lang.get("menu_editRunAttributes_tt"));

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

        JMenu run = new JMenu(Lang.get("menu_sim"));
        menuBar.add(run);
        run.add(editRunAttributes.createJMenuItem());
        run.addSeparator();
        run.add(runModelAction.createJMenuItem());
        run.add(runModelMicroAction.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(runToBreakAction.createJMenuItem());
        run.add(stoppedStateAction.createJMenuItem());
        run.add(runTests.createJMenuItem());
        run.addSeparator();
        run.add(speedTest.createJMenuItem());
        doStep.setEnabled(false);

        toolBar.add(runModelState.setIndicator(runModelAction.createJButtonNoText()));
        toolBar.add(runToBreakAction.createJButtonNoText());
        toolBar.add(stoppedStateAction.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runModelMicroState.setIndicator(runModelMicroAction.createJButtonNoText()));
        toolBar.add(doStep.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runTests.createJButtonNoText());
    }

    /**
     * starts the tests
     */
    public void startTests() {
        try {
            ArrayList<ValueTableDialog.TestSet> tsl = new ArrayList<>();
            for (VisualElement el : circuitComponent.getCircuit().getElements())
                if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION))
                    tsl.add(new ValueTableDialog.TestSet(
                            el.getElementAttributes().get(TestCaseElement.TESTDATA),
                            el.getElementAttributes().getCleanLabel()));

            if (tsl.isEmpty())
                throw new TestingDataException(Lang.get("err_noTestData"));

            windowPosManager.register("testResult", new ValueTableDialog(Main.this, Lang.get("msg_testResult"))
                    .addTestResult(tsl, circuitComponent.getCircuit(), library))
                    .setVisible(true);

            ensureModelIsStopped();
        } catch (NodeException | ElementNotFoundException | PinException | TestingDataException | RuntimeException e1) {
            showErrorAndStopModel(Lang.get("msg_runningTestError"), e1);
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
                    Model model = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);

                    if (model.isInvalidSignal())
                        JOptionPane.showMessageDialog(Main.this,
                                new LineBreaker().toHTML().breakLines(Lang.get("msg_invalidSignalsAnalysed")),
                                Lang.get("msg_warning"),
                                JOptionPane.WARNING_MESSAGE);

                    new TableDialog(Main.this, new ModelAnalyser(model).analyse(), library, shapeFactory, getBaseFileName())
                            .setVisible(true);
                    ensureModelIsStopped();
                } catch (PinException | NodeException | AnalyseException | ElementNotFoundException | BacktrackException | RuntimeException e1) {
                    showErrorAndStopModel(Lang.get("msg_analyseErr"), e1);
                }
            }
        }
                .setToolTip(Lang.get("menu_analyse_tt"))
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_synthesise")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TruthTable tt = new TruthTable(3).addResult();
                new TableDialog(Main.this, tt, library, shapeFactory, getBaseFileName()).setVisible(true);
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

    }

    private void orderMeasurements() {
        try {
            Model m = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
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
        } catch (NodeException | PinException | ElementNotFoundException | RuntimeException e) {
            showErrorAndStopModel(Lang.get("msg_errorCreatingModel"), e);
        }
    }

    private void setupStates() {
        stoppedState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                clearModelDescription();
                circuitComponent.setModeAndReset(false, NoSync.INST);
                doStep.setEnabled(false);
                stoppedState.getAction().setEnabled(false);
                runToBreakAction.setEnabled(false);
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
                stoppedState.getAction().setEnabled(true);
                if (createAndStartModel(false, ModelEvent.MICROSTEP, null))
                    circuitComponent.setManualChangeObserver(new MicroStepObserver(model));
            }
        });
    }

    private void clearModelDescription() {
        if (model != null)
            model.close();

        modelCreator = null;
        model = null;
    }

    private boolean createAndStartModel(boolean globalRunClock, ModelEvent updateEvent, ModelModifier modelModifier) {
        try {
            circuitComponent.removeHighLighted();

            modelCreator = new ModelCreator(circuitComponent.getCircuit(), library);

            if (model != null) {
                model.close();
                circuitComponent.getCircuit().clearState();
            }

            model = modelCreator.createModel(true);
            model.setWindowPosManager(windowPosManager);

            statusLabel.setText(Lang.get("msg_N_nodes", model.size()));

            realTimeClockRunning = false;
            modelSync = null;
            if (globalRunClock)
                for (Clock c : model.getClocks())
                    if (c.getFrequency() > 0) {
                        if (modelSync == null)
                            modelSync = new LockSync();
                        model.addObserver(new RealTimeClock(model, c, timerExecutor, this, modelSync, this));
                        realTimeClockRunning = true;
                    }
            if (modelSync == null)
                modelSync = NoSync.INST;

            circuitComponent.setModeAndReset(true, modelSync);

            if (realTimeClockRunning) {
                // if clock is running, enable automatic update of gui
                GuiModelObserver gmo = new GuiModelObserver(circuitComponent, updateEvent);
                modelCreator.connectToGui(gmo);
                model.addObserver(gmo);
            } else
                // all repainting is initiated by user actions!
                modelCreator.connectToGui(null);

            doStep.setEnabled(false);
            runToBreakAction.setEnabled(!realTimeClockRunning && model.isFastRunModel());

            ElementAttributes settings = circuitComponent.getCircuit().getAttributes();
            List<String> ordering = circuitComponent.getCircuit().getMeasurementOrdering();
            if (settings.get(Keys.SHOW_DATA_TABLE))
                windowPosManager.register("probe", new ProbeDialog(this, model, updateEvent, ordering, modelSync)).setVisible(true);

            if (settings.get(Keys.SHOW_DATA_GRAPH))
                windowPosManager.register("dataSet", GraphDialog.createLiveDialog(this, model, updateEvent == ModelEvent.MICROSTEP, ordering, modelSync)).setVisible(true);
            if (settings.get(Keys.SHOW_DATA_GRAPH_MICRO))
                windowPosManager.register("dataSetMicro", GraphDialog.createLiveDialog(this, model, true, ordering, modelSync)).setVisible(true);

            if (modelModifier != null)
                modelModifier.preInit(model);

            model.init();

            return true;
        } catch (NodeException | PinException | RuntimeException | ElementNotFoundException e) {
            showErrorAndStopModel(Lang.get("msg_errorCreatingModel"), e);
        }
        return false;
    }

    @Override
    public void showErrorAndStopModel(String message, Exception cause) {
        SwingUtilities.invokeLater(() -> {
            if (cause instanceof NodeException) {
                NodeException e = (NodeException) cause;
                circuitComponent.addHighLightedWires(e.getValues());
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
            circuitComponent.repaintNeeded();
            new ErrorMessage(message).addCause(cause).show(Main.this);
            stoppedState.enter();
        });
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
        return circuitComponent.getCircuit().isModified();
    }

    @Override
    public void saveChanges() {
        save.actionPerformed(null);
    }

    @Override
    public void open(File file) {
        if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
            loadFile(file, true, true);
        }
    }

    private void loadFile(File filename, boolean setLibraryRoot, boolean toPref) {
        try {
            setFilename(filename, toPref);
            if (setLibraryRoot) library.setRootFilePath(filename.getParentFile());
            Circuit circuit = Circuit.loadCircuit(filename, shapeFactory);
            circuitComponent.setCircuit(circuit);
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

            if (library.getRootFilePath() == null)
                library.setRootFilePath(filename.getParentFile());
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e).show();
        }
    }

    private void setFilename(File filename, boolean toPrefs) {
        modifiedPrefixVisible = circuitComponent.getCircuit().isModified();
        if (experimental && save != null)
            save.setActive(modifiedPrefixVisible);
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
    public void circuitHasChanged() {
        ensureModelIsStopped();
        if (modifiedPrefixVisible != circuitComponent.getCircuit().isModified())
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

    private class FullStepObserver implements Observer {
        private final Model model;

        FullStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            try {
                modelSync.accessNEx(() -> {
                    model.fireManualChangeEvent();
                    model.doStep();
                });
                circuitComponent.repaintNeeded();
            } catch (NodeException | RuntimeException e) {
                showErrorAndStopModel(Lang.get("msg_errorCalculatingStep"), e);
            }
        }
    }

    private class MicroStepObserver implements Observer {
        private final Model model;

        MicroStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            modelCreator.addNodeElementsTo(model.nodesToUpdate(), circuitComponent.getHighLighted());
            model.fireManualChangeEvent();
            circuitComponent.repaintNeeded();
            doStep.setEnabled(model.needsUpdate());
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
                        setDebug(false);
                        windowPosManager.closeAll();
                        runModelState.enter(false, gifExporter);
                        circuitComponent.repaintNeeded();
                    }
            );
        }
    }


    private class RunModelState extends State {
        @Override
        public void enter() {
            enter(true, null);
        }

        void enter(boolean runRealTime, ModelModifier modelModifier) {
            super.enter();
            stoppedState.getAction().setEnabled(true);
            if (createAndStartModel(runRealTime, ModelEvent.STEP, modelModifier))
                circuitComponent.setManualChangeObserver(new FullStepObserver(model));
        }
    }

    //***********************
    // remote interface start
    //***********************

    private static class AddressPicker {
        private long address;

        private void getProgramROMAddress(Model model) {
            List<ROM> roms = model.findNode(ROM.class, ROM::isProgramMemory);
            if (roms.size() == 1)
                address = roms.get(0).getRomAddress();
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

    private void setDebug(boolean debug) {
        ElementAttributes settings = circuitComponent.getCircuit().getAttributes();
        settings.set(Keys.SHOW_DATA_TABLE, debug);
    }

    @Override
    public void start(File romHex) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            setDebug(false);
            windowPosManager.closeAll();
            runModelState.enter(true, new RomLoader(romHex));
            circuitComponent.repaintNeeded();
        });
    }

    @Override
    public void debug(File romHex) throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            setDebug(true);
            runModelState.enter(false, new RomLoader(romHex));
            circuitComponent.repaintNeeded();
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
                        clkVal.setBool(!clkVal.getBool());
                        try {
                            model.doStep();
                            if (clkVal.getBool()) {
                                clkVal.setBool(!clkVal.getBool());
                                model.doStep();
                            }
                            circuitComponent.repaintNeeded();
                            addressPicker.getProgramROMAddress(model);
                        } catch (NodeException | RuntimeException e) {
                            showErrorAndStopModel(Lang.get("err_remoteExecution"), e);
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
                if (model != null && model.isFastRunModel() && !realTimeClockRunning)
                    runToBreakAction.actionPerformed(null);
                addressPicker.getProgramROMAddress(model);
            });
            return addressPicker.getAddressString();
        } catch (InterruptedException | InvocationTargetException e) {
            throw new RemoteException("error performing a run to break " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        SwingUtilities.invokeLater(() -> {
            ensureModelIsStopped();
            circuitComponent.repaintNeeded();
        });
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
        try { // enforce MetalLookAndFeel
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | UnsupportedLookAndFeelException | IllegalAccessException e) {
            e.printStackTrace();
        }
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        URL.setURLStreamHandlerFactory(ElementHelpDialog.createURLStreamHandlerFactory());
        FormatToExpression.setDefaultFormat(Settings.getInstance().get(Keys.SETTINGS_EXPRESSION_FORMAT));

        MainBuilder builder = new MainBuilder();
        for (String s : args) {
            if (s.equals("experimental")) experimental = true;
            if (s.toLowerCase().endsWith(".dig")) builder.setFileToOpen(new File(s));
        }
        SwingUtilities.invokeLater(() -> {
            Main main = builder.build();
            try {
                new RemoteSever(new DigitalHandler(main)).start(41114);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> main.statusLabel.setText(Lang.get("err_portIsInUse")));
            }
            main.setVisible(true);
        });
    }

    /**
     * Builder to create a Main-Window
     */
    public static class MainBuilder {
        private File fileToOpen;
        private Component parent;
        private ElementLibrary library;
        private Circuit circuit;
        private boolean allowAllFileActions = true;
        private File baseFileName;
        private boolean keepPrefMainFile;

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
        public MainBuilder setParent(Component parent) {
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
        private Main build() {
            return new Main(this);
        }

        /**
         * Opens the frame using SwingUtilities.invokeLater
         */
        public void openLater() {
            SwingUtilities.invokeLater(() -> build().setVisible(true));
        }

    }
}
