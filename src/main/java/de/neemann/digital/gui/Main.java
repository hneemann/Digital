package de.neemann.digital.gui;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.builder.PinMap;
import de.neemann.digital.builder.PinMapException;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.ElementOrder;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.*;
import de.neemann.digital.gui.components.data.DataSetDialog;
import de.neemann.digital.gui.components.expression.ExpressionDialog;
import de.neemann.digital.gui.components.listing.ROMListingDialog;
import de.neemann.digital.gui.components.table.TableDialog;
import de.neemann.digital.gui.remote.DigitalHandler;
import de.neemann.digital.gui.remote.RemoteSever;
import de.neemann.digital.gui.state.State;
import de.neemann.digital.gui.state.StateManager;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * The main frame of the Digital Simulator
 *
 * @author hneemann
 */
public class Main extends JFrame implements ClosingWindowListener.ConfirmSave, ErrorStopper, FileHistory.OpenInterface, DigitalRemoteInterface {
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();
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
    private static final Icon ICON_STEP = IconCreator.create("media-seek-forward.png");
    private static final Icon ICON_ELEMENT = IconCreator.create("preferences-system.png");
    private static final Icon ICON_NEW = IconCreator.create("document-new.png");
    private static final Icon ICON_OPEN = IconCreator.create("document-open.png");
    private static final Icon ICON_OPEN_WIN = IconCreator.create("document-open-new.png");
    private static final Icon ICON_SAVE = IconCreator.create("document-save.png");
    private static final Icon ICON_SAVE_AS = IconCreator.create("document-save-as.png");
    private static final Icon ICON_FAST = IconCreator.create("media-skip-forward.png");
    private static final Icon ICON_EXPAND = IconCreator.create("View-zoom-fit.png");
    private static final Icon ICON_ZOOMIN = IconCreator.create("View-zoom-in.png");
    private static final Icon ICON_ZOOMOUT = IconCreator.create("View-zoom-out.png");
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private ToolTipAction doStep;
    private ToolTipAction runToBreakAction;
    private final ElementLibrary library;
    private final LibrarySelector librarySelector;
    private final ShapeFactory shapeFactory;
    private final SavedListener savedListener;
    private final JLabel statusLabel;
    private final StateManager stateManager = new StateManager();
    private final ElementAttributes settings = new ElementAttributes();
    private final ScheduledThreadPoolExecutor timerExecuter = new ScheduledThreadPoolExecutor(1);
    private final WindowPosManager windowPosManager = new WindowPosManager();

    private File lastFilename;
    private File filename;
    private FileHistory fileHistory;

    private Model model;
    private ModelCreator modelCreator;
    private boolean realtimeClockRunning;

    private State elementState;
    private RunModelState runModelState;
    private State runModelMicroState;

    private Main() {
        this(null, null, null);
    }

    /**
     * Creates a new instance
     *
     * @param parent        the parent component
     * @param fileToOpen    a file to open
     * @param savedListener a listener which is notified if the file is changed on disk
     */
    public Main(Component parent, File fileToOpen, SavedListener savedListener) {
        this(parent, fileToOpen, savedListener, null);
    }

    /**
     * Creates a new instance
     *
     * @param parent  the parent component
     * @param circuit circuit to show
     */
    public Main(Component parent, Circuit circuit) {
        this(parent, null, null, circuit);
    }

    /**
     * Creates a new instance
     *
     * @param parent        the parent component
     * @param fileToOpen    a file to open
     * @param savedListener a listener which is notified if the file is changed on disk
     * @param circuit       circuit to show
     */
    private Main(Component parent, File fileToOpen, SavedListener savedListener, Circuit circuit) {
        super(Lang.get("digital"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImages(IconCreator.createImages("icon32.png", "icon64.png", "icon128.png"));
        this.savedListener = savedListener;

        library = new ElementLibrary();
        shapeFactory = new ShapeFactory(library, Settings.getInstance().get(Keys.SETTINGS_IEEE_SHAPES));

        fileHistory = new FileHistory(this);

        final boolean normalMode = savedListener == null;

        if (circuit != null) {
            circuitComponent = new CircuitComponent(library, shapeFactory, savedListener);
            SwingUtilities.invokeLater(() -> circuitComponent.setCircuit(circuit));
        } else {
            circuitComponent = new CircuitComponent(library, shapeFactory, savedListener);
            if (fileToOpen != null) {
                SwingUtilities.invokeLater(() -> loadFile(fileToOpen, false));
            } else {
                File name = fileHistory.getMostRecent();
                if (name != null) {
                    SwingUtilities.invokeLater(() -> loadFile(name, false));
                }
            }
        }

        getContentPane().add(circuitComponent);

        statusLabel = new JLabel(" ");
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        addWindowListener(new ClosingWindowListener(this, this));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                clearModelDescription(); // stop model timer if running
                timerExecuter.shutdown();
            }
        });

        setupStates();

        JMenuBar menuBar = new JMenuBar();
        JToolBar toolBar = new JToolBar();

        save = createFileMenu(menuBar, toolBar, normalMode);
        toolBar.addSeparator();

        createViewMenu(menuBar, toolBar);

        toolBar.addSeparator();

        ToolTipAction elementStateAction = elementState.createToolTipAction(Lang.get("menu_element"), ICON_ELEMENT).setToolTip(Lang.get("menu_element_tt"));

        createEditMenu(menuBar, elementStateAction);

        toolBar.add(elementState.setIndicator(elementStateAction.createJButtonNoText()));
        toolBar.add(circuitComponent.getDeleteAction().createJButtonNoText());
        toolBar.addSeparator();

        createStartMenu(menuBar, toolBar);

        createAnalyseMenu(menuBar);

        toolBar.addSeparator();

        librarySelector = new LibrarySelector(library, shapeFactory, elementState);
        menuBar.add(librarySelector.buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(menuBar);
        InfoDialog.getInstance().addToFrame(this, MESSAGE);

        setPreferredSize(new Dimension(1024, 768));
        pack();
        setLocationRelativeTo(parent);
    }

    private void createViewMenu(JMenuBar menuBar, JToolBar toolBar) {
        ToolTipAction maximize = new ToolTipAction(Lang.get("menu_maximize"), ICON_EXPAND) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.fitCircuit();
            }
        };
        ToolTipAction zoomIn = new ToolTipAction(Lang.get("menu_zoomIn"), ICON_ZOOMIN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.scaleCircuit(1.25);
            }
        };
        ToolTipAction zoomOut = new ToolTipAction(Lang.get("menu_zoomOut"), ICON_ZOOMOUT) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.scaleCircuit(0.8);
            }
        };
        toolBar.add(zoomIn.createJButtonNoText());
        toolBar.add(zoomOut.createJButtonNoText());
        toolBar.add(maximize.createJButtonNoText());

        JMenu view = new JMenu(Lang.get("menu_view"));
        menuBar.add(view);
        view.add(maximize.createJMenuItem());
        view.add(zoomOut.createJMenuItem());
        view.add(zoomIn.createJMenuItem());
    }

    /**
     * Creates the file menu and adds it to menu and toolbar
     *
     * @param menuBar    the menuBar
     * @param toolBar    the toolBar
     * @param normalMode if false, menu is added in nested mode
     * @return the save action
     */
    private ToolTipAction createFileMenu(JMenuBar menuBar, JToolBar toolBar, boolean normalMode) {
        ToolTipAction newFile = new ToolTipAction(Lang.get("menu_new"), ICON_NEW) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    setFilename(null, true);
                    circuitComponent.setCircuit(new Circuit());
                }
            }
        }.setActive(normalMode);

        ToolTipAction open = new ToolTipAction(Lang.get("menu_open"), ICON_OPEN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    JFileChooser fc = getJFileChooser(lastFilename);
                    if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                        loadFile(fc.getSelectedFile(), true);
                    }
                }
            }
        }.setActive(normalMode);

        ToolTipAction openWin = new ToolTipAction(Lang.get("menu_openWin"), ICON_OPEN_WIN) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getJFileChooser(lastFilename);
                if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    Main m = new Main(Main.this, fc.getSelectedFile(), null);
                    m.setLocationRelativeTo(Main.this);
                    m.setVisible(true);
                }
            }
        }.setToolTip(Lang.get("menu_openWin_tt")).setActive(normalMode);

        JMenu openRecent = new JMenu(Lang.get("menu_openRecent"));
        fileHistory.setMenu(openRecent);
        openRecent.setEnabled(normalMode);

        ToolTipAction saveas = new ToolTipAction(Lang.get("menu_saveAs"), ICON_SAVE_AS) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getJFileChooser(lastFilename);
                if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    saveFile(fc.getSelectedFile(), normalMode);
                }
            }
        }.setActive(normalMode);

        ToolTipAction save = new ToolTipAction(Lang.get("menu_save"), ICON_SAVE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveas.actionPerformed(e);
                else
                    saveFile(filename, normalMode);
            }
        };

        JMenu export = new JMenu(Lang.get("menu_export"));
        export.add(new ExportAction(Lang.get("menu_exportSVG"), "svg", GraphicSVGIndex::new));
        export.add(new ExportAction(Lang.get("menu_exportSVGLaTex"), "svg", GraphicSVGLaTeX::new));
        export.add(new ExportAction(Lang.get("menu_exportPNGSmall"), "png", (out, min, max) -> GraphicsImage.create(out, min, max, "PNG", 1)));
        export.add(new ExportAction(Lang.get("menu_exportPNGLarge"), "png", (out, min, max) -> GraphicsImage.create(out, min, max, "PNG", 2)));

        JMenu file = new JMenu(Lang.get("menu_file"));
        menuBar.add(file);
        file.add(newFile);
        file.add(openRecent);
        file.add(open);
        file.add(openWin);
        file.add(save);
        file.add(saveas);
        file.add(export);

        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());

        return save;
    }

    /**
     * Creates the edit menu
     *
     * @param menuBar            the menu bar
     * @param elementStateAction state action to add to menu
     */
    private void createEditMenu(JMenuBar menuBar, ToolTipAction elementStateAction) {
        JMenu edit = new JMenu(Lang.get("menu_edit"));
        menuBar.add(edit);

        ToolTipAction orderInputs = new ToolTipAction(Lang.get("menu_orderInputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementOrder o = new ElementOrder(circuitComponent.getCircuit(),
                        element -> element.equalsDescription(In.DESCRIPTION)
                                || element.equalsDescription(Clock.DESCRIPTION));
                new ElementOrderer<>(Main.this, Lang.get("menu_orderInputs"), o).showDialog();
            }
        }.setToolTip(Lang.get("menu_orderInputs_tt"));

        ToolTipAction orderOutputs = new ToolTipAction(Lang.get("menu_orderOutputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementOrder o = new ElementOrder(circuitComponent.getCircuit(),
                        element -> element.equalsDescription(Out.DESCRIPTION)
                                || element.equalsDescription(Out.LEDDESCRIPTION));
                new ElementOrderer<>(Main.this, Lang.get("menu_orderOutputs"), o).showDialog();
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
                circuitComponent.getCircuit().editAttributes(Main.this);
            }
        }.setToolTip(Lang.get("menu_editAttributes_tt"));

        ToolTipAction editSettings = new ToolTipAction(Lang.get("menu_editSettings")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (new AttributeDialog(Main.this, Settings.SETTINGS_KEYS, Settings.getInstance().getAttributes()).showDialog()) {
                    Lang.setLanguage(Settings.getInstance().getAttributes().get(Keys.SETTINGS_LANGUAGE));
                    JOptionPane.showMessageDialog(Main.this, Lang.get("msg_restartNeeded"));
                }
            }
        }.setToolTip(Lang.get("menu_editSettings_tt"));

        edit.add(editAttributes.createJMenuItem());
        edit.addSeparator();
        edit.add(elementStateAction.createJMenuItem());
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());
        edit.add(orderMeasurements.createJMenuItem());
        edit.addSeparator();

        JMenuItem copyItem = new JMenuItem(circuitComponent.getCopyAction());
        copyItem.setAccelerator(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        edit.add(copyItem);
        JMenuItem pasteItem = new JMenuItem(circuitComponent.getPasteAction());
        pasteItem.setAccelerator(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        edit.add(pasteItem);
        JMenuItem rotateItem = new JMenuItem(circuitComponent.getRotateAction());
        rotateItem.setAccelerator(KeyStroke.getKeyStroke('R'));
        edit.add(rotateItem);
        edit.addSeparator();
        edit.add(editSettings.createJMenuItem());
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
                    circuitComponent.repaint();
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
                    circuitComponent.repaint();
                    statusLabel.setText(Lang.get("stat_clocks", i));
                } catch (NodeException e1) {
                    elementState.enter();
                    new ErrorMessage(Lang.get("msg_fastRunError")).addCause(e1).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_fast_tt")).setActive(false);

        ToolTipAction speedTest = new ToolTipAction(Lang.get("menu_speedTest")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);

                    SpeedTest speedTest = new SpeedTest(model);
                    double frequency = speedTest.calculate();
                    circuitComponent.getCircuit().clearState();
                    JOptionPane.showMessageDialog(Main.this, Lang.get("msg_frequency_N", frequency));
                } catch (Exception e1) {
                    new ErrorMessage("SpeedTestError").addCause(e1).show();
                }
            }
        }.setToolTip(Lang.get("menu_speedTest_tt"));

        ToolTipAction editRunAttributes = new ToolTipAction(Lang.get("menu_editRunAttributes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new AttributeDialog(Main.this, ATTR_LIST, settings).showDialog();
            }
        }.setToolTip(Lang.get("menu_editRunAttributes_tt"));

        JMenu run = new JMenu(Lang.get("menu_run"));
        menuBar.add(run);
        run.add(editRunAttributes.createJMenuItem());
        run.addSeparator();
        run.add(runModelAction.createJMenuItem());
        run.add(runModelMicroAction.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(runToBreakAction.createJMenuItem());
        run.addSeparator();
        run.add(speedTest.createJMenuItem());
        doStep.setEnabled(false);

        toolBar.add(runModelState.setIndicator(runModelAction.createJButtonNoText()));
        toolBar.add(runToBreakAction.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runModelMicroState.setIndicator(runModelMicroAction.createJButtonNoText()));
        toolBar.add(doStep.createJButtonNoText());
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
                    new TableDialog(Main.this, new ModelAnalyser(model).analyse(), shapeFactory, filename)
                            .setPinMap(new PinMap().addModel(model))
                            .setVisible(true);
                    elementState.enter();
                } catch (PinException | PinMapException | NodeException | AnalyseException e1) {
                    showErrorAndStopModel(Lang.get("msg_annalyseErr"), e1);
                }
            }
        }
                .setToolTip(Lang.get("menu_analyse_tt"))
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_synthesise")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                TruthTable tt = new TruthTable(3).addResult();
                new TableDialog(Main.this, tt, shapeFactory, null).setVisible(true);
                elementState.enter();
            }
        }
                .setToolTip(Lang.get("menu_synthesise_tt"))
                .createJMenuItem());

        analyse.add(new ToolTipAction(Lang.get("menu_expression")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ExpressionDialog(Main.this, shapeFactory).setVisible(true);
            }
        }
                .setToolTip(Lang.get("menu_expression_tt"))
                .createJMenuItem());

    }

    private void orderMeasurements() {
        try {
            Model m = new ModelCreator(circuitComponent.getCircuit(), library).createModel(false);
            elementState.enter();
            ArrayList<String> names = new ArrayList<>();
            for (Signal s : m.getSignals())
                names.add(s.getName());
            new OrderMerger<String, String>(circuitComponent.getCircuit().getMeasurementOrdering()).order(names);
            ElementOrderer.ListOrder<String> o = new ElementOrderer.ListOrder<>(names);
            if (new ElementOrderer<>(Main.this, Lang.get("menu_orderMeasurements"), o)
                    .addOkButton()
                    .showDialog()) {
                circuitComponent.getCircuit().setMeasurementOrdering(names);
            }
        } catch (Exception e1) {
            showErrorAndStopModel(Lang.get("msg_errorCreatingModel"), e1);
        }
    }

    private void setupStates() {
        elementState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                clearModelDescription();
                circuitComponent.setModeAndReset(false);
                doStep.setEnabled(false);
                runToBreakAction.setEnabled(false);
            }

        });
        runModelState = stateManager.register(new RunModelState());
        runModelMicroState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                if (createAndStartModel(false, ModelEvent.MICROSTEP))
                    circuitComponent.setManualChangeObserver(new MicroStepObserver(model));
            }
        });
    }

    /**
     * Starts the main app
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        experimental = args.length == 1 && args[0].equals("experimental");
        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            try {
                new RemoteSever(new DigitalHandler(main)).start(41114);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> main.statusLabel.setText(Lang.get("err_portIsInUse")));
            }
            main.setVisible(true);
        });
    }

    private void clearModelDescription() {
        circuitComponent.removeHighLighted();
        if (model != null)
            model.close();

        modelCreator = null;
        model = null;
    }

    private boolean createAndStartModel(boolean globalRunClock, ModelEvent updateEvent) {
        try {
            circuitComponent.removeHighLighted();
            circuitComponent.setModeAndReset(true);

            modelCreator = new ModelCreator(circuitComponent.getCircuit(), library);

            if (model != null)
                model.close();

            model = modelCreator.createModel(true);

            statusLabel.setText(Lang.get("msg_N_nodes", model.size()));

            realtimeClockRunning = false;
            if (globalRunClock)
                for (Clock c : model.getClocks())
                    if (c.getFrequency() > 0) {
                        model.addObserver(new RealTimeClock(model, c, timerExecuter, this));
                        realtimeClockRunning = true;
                    }

            if (realtimeClockRunning) {
                // if clock is running, enable automatic update of gui
                GuiModelObserver gmo = new GuiModelObserver(circuitComponent, updateEvent);
                modelCreator.connectToGui(gmo);
                model.addObserver(gmo);
            } else
                // all repainting is initiated by user actions!
                modelCreator.connectToGui(null);

            doStep.setEnabled(false);
            runToBreakAction.setEnabled(!realtimeClockRunning && model.isFastRunModel());

            List<String> ordering = circuitComponent.getCircuit().getMeasurementOrdering();
            if (settings.get(Keys.SHOW_DATA_TABLE))
                windowPosManager.register("probe", new ProbeDialog(this, model, updateEvent, ordering)).setVisible(true);

            if (settings.get(Keys.SHOW_DATA_GRAPH))
                windowPosManager.register("dataset", new DataSetDialog(this, model, updateEvent == ModelEvent.MICROSTEP, ordering)).setVisible(true);
            if (settings.get(Keys.SHOW_DATA_GRAPH_MICRO))
                windowPosManager.register("datasetMicro", new DataSetDialog(this, model, true, ordering)).setVisible(true);

            int i = 0;
            for (ROM rom : model.findNode(ROM.class))
                if (rom.showListing())
                    try {
                        windowPosManager.register("rom" + (i++), new ROMListingDialog(this, rom)).setVisible(true);
                    } catch (IOException e) {
                        new ErrorMessage(Lang.get("msg_errorReadingListing_N0", rom.getHexFile().toString())).addCause(e).show(this);
                    }

            model.init();

            return true;
        } catch (NodeException | PinException | RuntimeException e) {
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
                circuitComponent.addHighLightedWires(e.getValue().asList());
            }
            circuitComponent.repaint();
            new ErrorMessage(message).addCause(cause).show(Main.this);
            elementState.enter();
        });
    }

    private static JFileChooser getJFileChooser(File filename) {
        File folder = null;
        if (filename != null)
            folder = filename.getParentFile();

        JFileChooser fileChooser = new JFileChooser(folder);
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
        loadFile(file, true);
    }

    private void loadFile(File filename, boolean toPrefs) {
        try {
            librarySelector.setFilePath(filename.getParentFile());
            Circuit circ = Circuit.loadCircuit(filename, shapeFactory);
            circuitComponent.setCircuit(circ);
            elementState.enter();
            setFilename(filename, toPrefs);
            statusLabel.setText(" ");
        } catch (Exception e) {
            circuitComponent.setCircuit(new Circuit());
            setFilename(null, false);
            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File filename, boolean toPrefs) {
        filename = checkSuffix(filename, "dig");
        try {
            circuitComponent.getCircuit().save(filename);
            if (savedListener != null)
                savedListener.saved(filename);
            elementState.enter();
            setFilename(filename, toPrefs);
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e).show();
        }
    }

    private void setFilename(File filename, boolean toPrefs) {
        this.filename = filename;
        if (filename != null) {
            this.lastFilename = filename;
            if (toPrefs)
                fileHistory.add(filename);
            setTitle(filename + " - " + Lang.get("digital"));
        } else
            setTitle(Lang.get("digital"));
    }

    /**
     * Adds the given suffix to the file
     *
     * @param filename filename
     * @param suffix   suffix
     * @return the file name with the given suffix
     */
    public static File checkSuffix(File filename, String suffix) {
        String name = filename.getName();
        int p = name.lastIndexOf('.');
        if (p >= 0)
            name = name.substring(0, p);
        return new File(filename.getParentFile(), name + "." + suffix);
    }

    private class FullStepObserver implements Observer {
        private final Model model;

        FullStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            try {
                model.fireManualChangeEvent();
                model.doStep();
                circuitComponent.repaint();
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
            circuitComponent.repaint();
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
            JFileChooser fc = new JFileChooser();
            if (filename != null)
                fc.setSelectedFile(checkSuffix(filename, suffix));

            fc.addChoosableFileFilter(new FileNameExtensionFilter(name, suffix));
            if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                try (OutputStream out = new FileOutputStream(checkSuffix(fc.getSelectedFile(), suffix))) {
                    new Export(circuitComponent.getCircuit(), exportFactory).export(out);
                } catch (IOException e1) {
                    new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e1).show(Main.this);
                }
            }
        }
    }

    private class RunModelState extends State {
        @Override
        public void enter() {
            enter(true);
        }

        void enter(boolean runRealTime) {
            super.enter();
            if (createAndStartModel(runRealTime, ModelEvent.STEP))
                circuitComponent.setManualChangeObserver(new FullStepObserver(model));
        }
    }

    //**********************
    // remote interface start
    //**********************

    @Override
    public boolean loadRom(File file) {
        boolean found = false;
        ArrayList<VisualElement> el = circuitComponent.getCircuit().getElements();
        for (VisualElement e : el) {
            if (e.equalsDescription(ROM.DESCRIPTION)) {
                ElementAttributes attr = e.getElementAttributes();
                if (attr.get(Keys.AUTO_RELOAD_ROM)) {
                    attr.setFile(ROM.LAST_DATA_FILE_KEY, file);
                    found = true;
                }
            }
        }
        return found;
    }

    @Override
    public void doSingleStep() {
        if (model != null && !realtimeClockRunning) {
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
                    circuitComponent.repaint();
                } catch (NodeException e) {
                    showErrorAndStopModel(Lang.get("err_remoteExecution"), e);
                }
            }
        }
    }

    @Override
    public void runToBreak() {
        if (model != null && model.isFastRunModel() && !realtimeClockRunning)
            runToBreakAction.actionPerformed(null);
    }

    @Override
    public boolean start() {
        runModelState.enter(false);
        circuitComponent.repaint();
        return true;
    }

    @Override
    public void stop() {
        elementState.enter();
        circuitComponent.repaint();
    }
    //**********************
    // remote interface end
    //**********************

}
