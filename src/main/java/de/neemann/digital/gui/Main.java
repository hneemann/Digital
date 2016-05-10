package de.neemann.digital.gui;

import de.neemann.digital.analyse.AnalyseException;
import de.neemann.digital.analyse.ModelAnalyser;
import de.neemann.digital.analyse.TruthTable;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.ElementOrder;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.*;
import de.neemann.digital.gui.components.data.DataSetDialog;
import de.neemann.digital.gui.components.listing.ROMListingDialog;
import de.neemann.digital.gui.components.table.TableFrame;
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
import java.util.prefs.Preferences;

/**
 * The main frame of the Digital Simulator
 *
 * @author hneemann
 */
public class Main extends JFrame implements ClosingWindowListener.ConfirmSave, ErrorStopper {
    private static final Preferences PREFS = Preferences.userRoot().node("dig");
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();

    static {
        ATTR_LIST.add(Keys.SHOW_DATA_TABLE);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH_MICRO);
        ATTR_LIST.add(Keys.SHOW_LISTING);
    }

    private static final String MESSAGE = Lang.get("message");
    private static final Icon ICON_RUN = IconCreator.create("run.gif");
    private static final Icon ICON_MICRO = IconCreator.create("micro.gif");
    private static final Icon ICON_STEP = IconCreator.create("step.gif");
    private static final Icon ICON_ELEMENT = IconCreator.create("element.gif");
    private static final Icon ICON_NEW = IconCreator.create("New24.gif");
    private static final Icon ICON_OPEN = IconCreator.create("Open24.gif");
    private static final Icon ICON_OPEN_WIN = IconCreator.create("OpenNew24.gif");
    private static final Icon ICON_SAVE = IconCreator.create("Save24.gif");
    private static final Icon ICON_SAVE_AS = IconCreator.create("SaveAs24.gif");
    private static final Icon ICON_FAST = IconCreator.create("FastForward24.gif");
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private ToolTipAction doStep;
    private ToolTipAction runToBreak;
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
    private Model model;
    private ModelDescription modelDescription;

    private State elementState;
    private State runModelState;
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
        shapeFactory = new ShapeFactory(library);

        final boolean normalMode = savedListener == null;

        if (circuit != null) {
            circuitComponent = new CircuitComponent(library, shapeFactory, savedListener);
            SwingUtilities.invokeLater(() -> circuitComponent.setCircuit(circuit));
        } else {
            circuitComponent = new CircuitComponent(library, shapeFactory, savedListener);
            if (fileToOpen != null) {
                SwingUtilities.invokeLater(() -> loadFile(fileToOpen, false));
            } else {
                String name = PREFS.get("name", null);
                if (name != null) {
                    SwingUtilities.invokeLater(() -> loadFile(new File(name), false));
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
                ElementOrder o = new ElementOrder(circuitComponent.getCircuit(), "In");
                new ElementOrderer<>(Main.this, Lang.get("menu_orderInputs"), o).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_orderInputs_tt"));

        ToolTipAction orderOutputs = new ToolTipAction(Lang.get("menu_orderOutputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ElementOrder o = new ElementOrder(circuitComponent.getCircuit(), "Out");
                new ElementOrderer<>(Main.this, Lang.get("menu_orderOutputs"), o).setVisible(true);
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

        edit.add(elementStateAction.createJMenuItem());
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());
        edit.add(orderMeasurements.createJMenuItem());
        edit.add(editAttributes.createJMenuItem());
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
                    modelDescription.addNodeElementsTo(model.nodesToUpdate(), circuitComponent.getHighLighted());
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
        runToBreak = new ToolTipAction(Lang.get("menu_fast"), ICON_FAST) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int i = model.runToBreak();
                    circuitComponent.repaint();
                    statusLabel.setText(Lang.get("stat_clocks", i));
                } catch (NodeException e1) {
                    elementState.activate();
                    new ErrorMessage(Lang.get("msg_fastRunError")).addCause(e1).show(Main.this);
                }
            }
        }.setToolTip(Lang.get("menu_fast_tt")).setActive(false);

        ToolTipAction speedTest = new ToolTipAction(Lang.get("menu_speedTest")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelDescription(circuitComponent.getCircuit(), library).createModel(false);

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
        run.add(runModelAction.createJMenuItem());
        run.add(runModelMicroAction.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(runToBreak.createJMenuItem());
        run.add(speedTest.createJMenuItem());
        doStep.setEnabled(false);

        toolBar.add(runModelState.setIndicator(runModelAction.createJButtonNoText()));
        toolBar.add(runToBreak.createJButtonNoText());
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
                    Model model = new ModelDescription(circuitComponent.getCircuit(), library).createModel(false);
                    new TableFrame(Main.this, new ModelAnalyser(model).analyse(), shapeFactory).setVisible(true);
                    elementState.activate();
                } catch (PinException | NodeException | AnalyseException e1) {
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
                new TableFrame(Main.this, tt, shapeFactory).setVisible(true);
                elementState.activate();
            }
        }
                .setToolTip(Lang.get("menu_synthesise_tt"))
                .createJMenuItem());
    }

    private void orderMeasurements() {
        try {
            Model m = new ModelDescription(circuitComponent.getCircuit(), library).createModel(false);
            elementState.activate();
            ArrayList<String> names = new ArrayList<>();
            for (Model.Signal s : m.getSignals())
                names.add(s.getName());
            new OrderMerger<String, String>(circuitComponent.getCircuit().getMeasurementOrdering()).order(names);
            ElementOrderer.ListOrder<String> o = new ElementOrderer.ListOrder<>(names);
            new ElementOrderer<>(Main.this, Lang.get("menu_orderMeasurements"), o).setVisible(true);
            circuitComponent.getCircuit().setMeasurementOrdering(names);
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
                runToBreak.setEnabled(false);
            }

        });
        runModelState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                if (createAndStartModel(true, ModelEvent.STEP))
                    circuitComponent.setManualChangeObserver(new FullStepObserver(model));
            }
        });
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
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private void clearModelDescription() {
        circuitComponent.removeHighLighted();
        if (model != null)
            model.close();

        modelDescription = null;
        model = null;
    }

    private boolean createAndStartModel(boolean globalRunClock, ModelEvent updateEvent) {
        try {
            circuitComponent.removeHighLighted();
            circuitComponent.setModeAndReset(true);

            modelDescription = new ModelDescription(circuitComponent.getCircuit(), library);

            if (model != null)
                model.close();

            model = modelDescription.createModel(true);

            statusLabel.setText(Lang.get("msg_N_nodes", model.size()));

            boolean runClock = false;
            if (globalRunClock)
                for (Clock c : model.getClocks())
                    if (c.getFrequency() > 0) {
                        model.addObserver(new RealTimeClock(model, c, timerExecuter, this));
                        runClock = true;
                    }

            if (runClock) {
                // if clock is running, enable automatic update of gui
                GuiModelObserver gmo = new GuiModelObserver(circuitComponent, updateEvent);
                modelDescription.connectToGui(gmo);
                model.addObserver(gmo);
            } else
                // all repainting is initiated by user actions!
                modelDescription.connectToGui(null);

            doStep.setEnabled(false);
            runToBreak.setEnabled(!runClock && model.isFastRunModel());

            List<String> ordering = circuitComponent.getCircuit().getMeasurementOrdering();
            if (settings.get(Keys.SHOW_DATA_TABLE))
                windowPosManager.register("probe", new ProbeDialog(this, model, updateEvent, ordering)).setVisible(true);

            if (settings.get(Keys.SHOW_DATA_GRAPH))
                windowPosManager.register("dataset", new DataSetDialog(this, model, updateEvent == ModelEvent.MICROSTEP, ordering)).setVisible(true);
            if (settings.get(Keys.SHOW_DATA_GRAPH_MICRO))
                windowPosManager.register("datasetMicro", new DataSetDialog(this, model, true, ordering)).setVisible(true);

            if (settings.get(Keys.SHOW_LISTING)) {
                int i = 0;
                for (ROM rom : model.getRoms())
                    try {
                        windowPosManager.register("rom" + (i++), new ROMListingDialog(this, rom)).setVisible(true);
                    } catch (IOException e) {
                        new ErrorMessage(Lang.get("msg_errorReadingListing_N0", rom.getListFile().toString())).addCause(e).show(this);
                    }
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
                if (modelDescription != null)
                    modelDescription.addNodeElementsTo(e.getNodes(), circuitComponent.getHighLighted());
            } else if (cause instanceof PinException) {
                PinException e = (PinException) cause;
                circuitComponent.addHighLighted(e.getVisualElement());
                if (e.getNet() != null)
                    circuitComponent.addHighLighted(e.getNet().getWires());
            } else if (cause instanceof BurnException) {
                BurnException e = (BurnException) cause;
                circuitComponent.addHighLightedWires(new ObservableValue[]{e.getValue()});
            }
            circuitComponent.repaint();
            new ErrorMessage(message).addCause(cause).show(Main.this);
            elementState.activate();
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

    private void loadFile(File filename, boolean toPrefs) {
        try {
            librarySelector.setFilePath(filename.getParentFile());
            Circuit circ = Circuit.loadCircuit(filename, shapeFactory);
            circuitComponent.setCircuit(circ);
            elementState.activate();
            setFilename(filename, toPrefs);
            statusLabel.setText(" ");
        } catch (Exception e) {
            circuitComponent.setCircuit(new Circuit());
            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File filename, boolean toPrefs) {
        if (!filename.getName().endsWith(".dig"))
            filename = new File(filename.getPath() + ".dig");

        try {
            circuitComponent.getCircuit().save(filename);
            if (savedListener != null)
                savedListener.saved(filename);
            elementState.activate();
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
                PREFS.put("name", filename.getPath());
            setTitle(filename + " - " + Lang.get("digital"));
        } else
            setTitle(Lang.get("digital"));
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
            modelDescription.addNodeElementsTo(model.nodesToUpdate(), circuitComponent.getHighLighted());
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
            if (filename != null) {
                String name = filename.getName();
                int p = name.lastIndexOf('.');
                if (p >= 0)
                    name = name.substring(0, p);
                File f = new File(filename.getParentFile(), name + "." + suffix);
                fc.setSelectedFile(f);
            }
            fc.addChoosableFileFilter(new FileNameExtensionFilter(name, suffix));
            if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                try (OutputStream out = new FileOutputStream(fc.getSelectedFile())) {
                    new Export(circuitComponent.getCircuit(), exportFactory).export(out);
                } catch (IOException e1) {
                    new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e1).show(Main.this);
                }
            }
        }
    }
}
