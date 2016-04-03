package de.neemann.digital.gui;

import de.neemann.digital.core.*;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.PinOrder;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.model.RealTimeClock;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.gui.components.ProbeDialog;
import de.neemann.digital.gui.components.listing.ROMListingDialog;
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
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.prefs.Preferences;

/**
 * @author hneemann
 */
public class Main extends JFrame implements ClosingWindowListener.ConfirmSave {
    private static final Preferences prefs = Preferences.userRoot().node("dig");
    private static final String MESSAGE = Lang.get("message");
    private static final Icon iconRun = IconCreator.create("run.gif");
    private static final Icon iconMicro = IconCreator.create("micro.gif");
    private static final Icon iconStep = IconCreator.create("step.gif");
    private static final Icon iconElement = IconCreator.create("element.gif");
    private static final Icon iconSelect = IconCreator.create("Select24.gif");
    private static final Icon iconWire = IconCreator.create("wire.gif");
    private static final Icon iconNew = IconCreator.create("New24.gif");
    private static final Icon iconOpen = IconCreator.create("Open24.gif");
    private static final Icon iconOpenWin = IconCreator.create("OpenNew24.gif");
    private static final Icon iconSave = IconCreator.create("Save24.gif");
    private static final Icon iconSaveAs = IconCreator.create("SaveAs24.gif");
    private static final Icon iconFast = IconCreator.create("FastForward24.gif");
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private final ToolTipAction doStep;
    private final ToolTipAction runToBreak;
    private final ElementLibrary library;
    private final JCheckBoxMenuItem runClock;
    private final JCheckBoxMenuItem showProbes;
    private final JCheckBoxMenuItem showListing;
    private final JCheckBoxMenuItem traceEnable;
    private final LibrarySelector librarySelector;
    private final ShapeFactory shapeFactory;
    private final SavedListener savedListener;
    private final JLabel statusLabel;
    private final StateManager stateManager = new StateManager();
    private File lastFilename;
    private File filename;
    private Model model;
    private ModelDescription modelDescription;
    private ScheduledThreadPoolExecutor timerExecuter = new ScheduledThreadPoolExecutor(1);

    private State elementState;
    private State wireState;
    private State selectState;
    private State runModelState;
    private State runModelMicroState;

    private Main() {
        this(null, null, null);
    }

    public Main(Component parent, File fileToOpen, SavedListener savedListener) {
        super(Lang.get("digital"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setIconImages(IconCreator.createImages("icon32.png", "icon64.png", "icon128.png"));
        this.savedListener = savedListener;

        library = new ElementLibrary();
        shapeFactory = new ShapeFactory(library);

        final boolean normalMode = savedListener == null;

        Circuit cr = new Circuit();
        circuitComponent = new CircuitComponent(cr, library, shapeFactory);

        if (fileToOpen != null) {
            SwingUtilities.invokeLater(() -> loadFile(fileToOpen, false));
        } else {
            String name = prefs.get("name", null);
            if (name != null) {
                SwingUtilities.invokeLater(() -> loadFile(new File(name), false));
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

        JMenuBar bar = new JMenuBar();

        ToolTipAction newFile = new ToolTipAction(Lang.get("menu_new"), iconNew) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    setFilename(null, true);
                    circuitComponent.setCircuit(new Circuit());
                }
            }
        }.setActive(normalMode);

        ToolTipAction open = new ToolTipAction(Lang.get("menu_open"), iconOpen) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    JFileChooser fc = getjFileChooser(lastFilename);
                    if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                        loadFile(fc.getSelectedFile(), true);
                    }
                }
            }
        }.setActive(normalMode);

        ToolTipAction openWin = new ToolTipAction(Lang.get("menu_openWin"), iconOpenWin) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    JFileChooser fc = getjFileChooser(lastFilename);
                    if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                        Main m = new Main(Main.this, fc.getSelectedFile(), null);
                        m.setLocationRelativeTo(Main.this);
                        m.setVisible(true);
                    }
                }
            }
        }.setToolTip(Lang.get("menu_openWin_tt")).setActive(normalMode);


        ToolTipAction saveas = new ToolTipAction(Lang.get("menu_saveAs"), iconSaveAs) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser(lastFilename);
                if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    saveFile(fc.getSelectedFile(), normalMode);
                }
            }
        }.setActive(normalMode);

        save = new ToolTipAction(Lang.get("menu_save"), iconSave) {
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

        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(newFile);
        file.add(open);
        file.add(openWin);
        file.add(save);
        file.add(saveas);
        file.add(export);

        setupStates();

        JMenu edit = new JMenu(Lang.get("menu_edit"));
        bar.add(edit);

        ToolTipAction wireStateAction = wireState.createToolTipAction(Lang.get("menu_wire"), iconWire).setToolTip(Lang.get("menu_wire_tt"));
        ToolTipAction elementStateAction = elementState.createToolTipAction(Lang.get("menu_element"), iconElement).setToolTip(Lang.get("menu_element_tt"));
        ToolTipAction selectStateAction = selectState.createToolTipAction(Lang.get("menu_select"), iconSelect).setToolTip(Lang.get("menu_select_tt"));

        ToolTipAction orderInputs = new ToolTipAction(Lang.get("menu_orderInputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PinOrder o = new PinOrder(circuitComponent.getCircuit(), "In");
                new ElementOrderer<>(Main.this, Lang.get("menu_orderInputs"), o).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_orderInputs_tt"));

        ToolTipAction orderOutputs = new ToolTipAction(Lang.get("menu_orderOutputs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                PinOrder o = new PinOrder(circuitComponent.getCircuit(), "Out");
                new ElementOrderer<>(Main.this, Lang.get("menu_orderOutputs"), o).setVisible(true);
            }
        }.setToolTip(Lang.get("menu_orderOutputs_tt"));

        ToolTipAction editAttributes = new ToolTipAction(Lang.get("menu_editAttributes")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                circuitComponent.getCircuit().editAttributes(Main.this);
            }
        }.setToolTip(Lang.get("menu_editAttributes_tt"));


        edit.add(elementStateAction.createJMenuItem());
        edit.add(wireStateAction.createJMenuItem());
        edit.add(selectStateAction.createJMenuItem());
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());
        edit.add(editAttributes.createJMenuItem());


        JMenu run = new JMenu(Lang.get("menu_run"));
        bar.add(run);

        doStep = new ToolTipAction(Lang.get("menu_step"), iconStep) {
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

        ToolTipAction runModelAction = runModelState.createToolTipAction(Lang.get("menu_run"), iconRun)
                .setToolTip(Lang.get("menu_run_tt"));
        ToolTipAction runModelMicroAction = runModelMicroState.createToolTipAction(Lang.get("menu_micro"), iconMicro)
                .setToolTip(Lang.get("menu_micro_tt"));
        runToBreak = new ToolTipAction(Lang.get("menu_fast"), iconFast) {
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
                    Model model = new ModelDescription(circuitComponent.getCircuit(), library).createModel();

                    SpeedTest speedTest = new SpeedTest(model);
                    double frequency = speedTest.calculate();
                    circuitComponent.getCircuit().clearState();
                    JOptionPane.showMessageDialog(Main.this, "Frequency: " + frequency);
                } catch (Exception e1) {
                    new ErrorMessage("SpeedTestError").addCause(e1).show();
                }
            }
        }.setToolTip(Lang.get("menu_speedTest_tt"));

        showListing = new JCheckBoxMenuItem(Lang.get("menu_listing"));
        showListing.setToolTipText(Lang.get("menu_listing_tt"));
        showProbes = new JCheckBoxMenuItem(Lang.get("menu_probe"));
        showProbes.setToolTipText(Lang.get("menu_probe_tt"));
        traceEnable = new JCheckBoxMenuItem(Lang.get("menu_trace"));
        runClock = new JCheckBoxMenuItem(Lang.get("menu_runClock"));
        runClock.setToolTipText(Lang.get("menu_runClock_tt"));

        run.add(runModelAction.createJMenuItem());
        run.add(runModelMicroAction.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(runToBreak.createJMenuItem());
        //run.add(speedTest.createJMenuItem());
        run.add(showProbes);
        run.add(showListing);
        //run.add(traceEnable);
        run.add(runClock);
        doStep.setEnabled(false);

        JToolBar toolBar = new JToolBar();
        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(elementState.setIndicator(elementStateAction.createJButtonNoText()));
        toolBar.add(wireState.setIndicator(wireStateAction.createJButtonNoText()));
        toolBar.add(selectState.setIndicator(selectStateAction.createJButtonNoText()));
        toolBar.add(circuitComponent.getDeleteAction().createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runModelState.setIndicator(runModelAction.createJButtonNoText()));
        toolBar.add(runToBreak.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(runModelMicroState.setIndicator(runModelMicroAction.createJButtonNoText()));
        toolBar.add(doStep.createJButtonNoText());
        toolBar.addSeparator();

        librarySelector = new LibrarySelector(library, shapeFactory, elementState);
        bar.add(librarySelector.buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
        InfoDialog.getInstance().addToFrame(this, MESSAGE);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(parent);
    }

    private void setupStates() {
        elementState = stateManager.register(new ModeState(CircuitComponent.Mode.part));
        wireState = stateManager.register(new ModeState(CircuitComponent.Mode.wire));
        selectState = stateManager.register(new ModeState(CircuitComponent.Mode.select));
        runModelState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                createAndStartModel(runClock.isSelected(), ModelEvent.Event.STEP);
                circuitComponent.setManualChangeObserver(new FullStepObserver(model));
            }
        });
        runModelMicroState = stateManager.register(new State() {
            @Override
            public void enter() {
                super.enter();
                createAndStartModel(false, ModelEvent.Event.MICROSTEP);
                circuitComponent.setManualChangeObserver(new MicroStepObserver(model));
            }
        });
    }

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

    private void setModelDescription(ModelDescription md, boolean runClock) throws NodeException, PinException {
        modelDescription = md;

        if (model != null)
            model.close();

        model = modelDescription.createModel();
    }


    private void createAndStartModel(boolean runClock, ModelEvent.Event updateEvent) {
        try {
            circuitComponent.removeHighLighted();
            circuitComponent.setModeAndReset(CircuitComponent.Mode.running);

            setModelDescription(new ModelDescription(circuitComponent.getCircuit(), library), runClock);
            if (runClock) {
                // if clock is running, enable automatic update of gui
                GuiModelObserver gmo = new GuiModelObserver(circuitComponent, updateEvent);
                modelDescription.connectToGui(gmo);
                model.addObserver(gmo);
            } else
                // all repainting is initiated by user actions!
                modelDescription.connectToGui(null);

            if (runClock) {
                for (Clock c : model.getClocks())
                    model.addObserver(new RealTimeClock(model, c, timerExecuter));
            }

            runToBreak.setEnabled(!runClock && model.isFastRunModel());


            if (showProbes.isSelected())
                new ProbeDialog(this, model, updateEvent).setVisible(true);

            if (showListing.isSelected())
                for (ROM rom : model.getRoms())
                    try {
                        new ROMListingDialog(this, rom).setVisible(true);
                    } catch (IOException e) {
                        new ErrorMessage(Lang.get("msg_errorReadingListing_N0", rom.getListFile().toString())).addCause(e).show(this);
                    }


            model.init();

        } catch (NodeException e) {
            if (modelDescription != null) {
                if (e.getNodes() != null)
                    modelDescription.addNodeElementsTo(e.getNodes(), circuitComponent.getHighLighted());
                else
                    circuitComponent.addHighLightedWires(e.getValues());

                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).setComponent(Main.this));
            circuitComponent.setModeAndReset(CircuitComponent.Mode.part);
        } catch (PinException e) {
            if (modelDescription != null) {
                circuitComponent.addHighLighted(e.getVisualElement());
                if (e.getNet() != null)
                    circuitComponent.addHighLighted(e.getNet().getWires());
                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).setComponent(Main.this));
            circuitComponent.setModeAndReset(CircuitComponent.Mode.part);
        }
    }

    private static JFileChooser getjFileChooser(File filename) {
        JFileChooser fileChooser = new JFileChooser(filename == null ? null : filename.getParentFile());
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
                prefs.put("name", filename.getPath());
            setTitle(filename + " - " + Lang.get("digital"));
        } else
            setTitle(Lang.get("digital"));
    }

    private class ModeState extends State {
        private final CircuitComponent.Mode mode;

        ModeState(CircuitComponent.Mode mode) {
            this.mode = mode;
        }

        @Override
        public void enter() {
            super.enter();
            clearModelDescription();
            circuitComponent.setModeAndReset(mode);
            doStep.setEnabled(false);
        }
    }

    private class FullStepObserver implements Observer {
        private final Model model;

        FullStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            try {
                model.doStep();
                circuitComponent.repaint();
            } catch (Exception e) {
                SwingUtilities.invokeLater(
                        new ErrorMessage(Lang.get("msg_errorCalculatingStep")).addCause(e).setComponent(Main.this)
                );
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
            circuitComponent.repaint();
            doStep.setEnabled(model.needsUpdate());
        }
    }

    private class ExportAction extends ToolTipAction {
        private final String name;
        private final String suffix;
        private final Exporter exporter;

        ExportAction(String name, String suffix, Exporter exporter) {
            super(name);
            this.name = name;
            this.suffix = suffix;
            this.exporter = exporter;
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
                Circuit circuit = circuitComponent.getCircuit();
                GraphicMinMax minMax = new GraphicMinMax();
                circuit.drawTo(minMax);
                try {
                    Graphic gr = null;
                    try {
                        gr = exporter.create(fc.getSelectedFile(), minMax.getMin(), minMax.getMax());
                        circuit.drawTo(gr);
                    } finally {
                        if (gr instanceof Closeable)
                            ((Closeable) gr).close();
                    }
                } catch (IOException e1) {
                    new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e1).show(Main.this);
                }
            }
        }
    }
}
