package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SpeedTest;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.PinOrder;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelBuilder;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
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
    private static final Icon iconSelect = IconCreator.create("select.gif");
    private static final Icon iconWire = IconCreator.create("wire.gif");
    private static final Icon iconNew = IconCreator.create("New24.gif");
    private static final Icon iconOpen = IconCreator.create("Open24.gif");
    private static final Icon iconOpenWin = IconCreator.create("OpenNew24.gif");
    private static final Icon iconSave = IconCreator.create("Save24.gif");
    private static final Icon iconSaveAs = IconCreator.create("SaveAs24.gif");
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private final ElementLibrary library;
    private final ToolTipAction doStep;
    private final JCheckBoxMenuItem traceEnable;
    private final JCheckBoxMenuItem runClock;
    private final LibrarySelector librarySelector;
    private final ShapeFactory shapeFactory;
    private final SavedListener savedListener;
    private File lastFilename;
    private File filename;
    private Model model;
    private ModelDescription modelDescription;

    public Main() {
        this(null, null, null);
    }

    public Main(Component parent, File fileToOpen, SavedListener savedListener) {
        super(Lang.get("digital"));
        this.savedListener = savedListener;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        library = new ElementLibrary();
        shapeFactory = new ShapeFactory(library);

        boolean normalMode = savedListener == null;

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

        addWindowListener(new ClosingWindowListener(this, this));

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
                    saveFile(fc.getSelectedFile());
                }
            }
        }.setActive(normalMode);

        save = new ToolTipAction(Lang.get("menu_save"), iconSave) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveas.actionPerformed(e);
                else
                    saveFile(filename);
            }
        };

        JMenu file = new JMenu(Lang.get("menu_file"));
        bar.add(file);
        file.add(newFile);
        file.add(open);
        file.add(openWin);
        file.add(save);
        file.add(saveas);

        JMenu edit = new JMenu(Lang.get("menu_edit"));
        bar.add(edit);

        ToolTipAction wireMode = new ModeAction(Lang.get("menu_wire"), iconWire, CircuitComponent.Mode.wire).setToolTip(Lang.get("menu_wire_tt"));
        ToolTipAction partsMode = new ModeAction(Lang.get("menu_element"), iconElement, CircuitComponent.Mode.part).setToolTip(Lang.get("menu_element_tt"));
        ToolTipAction selectionMode = new ModeAction(Lang.get("menu_select"), iconSelect, CircuitComponent.Mode.select).setToolTip(Lang.get("menu_select_tt"));

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


        edit.add(partsMode.createJMenuItem());
        edit.add(wireMode.createJMenuItem());
        edit.add(selectionMode.createJMenuItem());
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());


        JMenu run = new JMenu(Lang.get("menu_run"));
        bar.add(run);

        doStep = new ToolTipAction(Lang.get("menu_step"), iconStep) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    model.doMicroStep(false);
                    modelDescription.highLight(model.nodesToUpdate());
                    circuitComponent.repaint(); // necessary to update the wires!
                    doStep.setEnabled(model.needsUpdate());
                } catch (Exception e1) {
                    SwingUtilities.invokeLater(
                            new ErrorMessage(Lang.get("msg_errorCalculatingStep")).addCause(e1).setComponent(Main.this)
                    );
                }
            }
        }.setToolTip(Lang.get("menu_step_tt"));

        ToolTipAction runModel = new ToolTipAction(Lang.get("menu_run"), iconRun) {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndStartModel(runClock.isSelected());
                circuitComponent.setManualChangeObserver(new FullStepObserver(model));
            }
        }.setToolTip(Lang.get("menu_run_tt"));

        ToolTipAction runModelMicro = new ToolTipAction(Lang.get("menu_micro"), iconMicro) {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndStartModel(false);
                circuitComponent.setManualChangeObserver(new MicroStepObserver(model));
            }
        }.setToolTip(Lang.get("menu_micro_tt"));

        ToolTipAction speedTest = new ToolTipAction(Lang.get("menu_speedTest")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Model model = new ModelBuilder(circuitComponent.getCircuit())
                            .build(library);

                    SpeedTest speedTest = new SpeedTest(model);
                    double frequency = speedTest.calculate();
                    JOptionPane.showMessageDialog(Main.this, "Frequency: " + frequency);
                } catch (Exception e1) {
                    new ErrorMessage("SpeedTestError").addCause(e1).show();
                }
            }
        }.setToolTip(Lang.get("menu_speedTest_tt"));

        traceEnable = new JCheckBoxMenuItem(Lang.get("menu_trace"));
        runClock = new JCheckBoxMenuItem(Lang.get("menu_runClock"), true);
        runClock.setToolTipText(Lang.get("menu_runClock_tt"));

        run.add(runModel.createJMenuItem());
        run.add(runModelMicro.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(speedTest.createJMenuItem());
        run.add(traceEnable);
        run.add(runClock);
        doStep.setEnabled(false);

        JToolBar toolBar = new JToolBar();
        toolBar.add(newFile.createJButtonNoText());
        toolBar.add(open.createJButtonNoText());
        toolBar.add(openWin.createJButtonNoText());
        toolBar.add(save.createJButtonNoText());
        toolBar.addSeparator();
        toolBar.add(partsMode.createJButtonNoText());
        toolBar.add(wireMode.createJButtonNoText());
        toolBar.add(selectionMode.createJButtonNoText());
        toolBar.add(runModel.createJButtonNoText());
        toolBar.add(runModelMicro.createJButtonNoText());
        toolBar.add(doStep.createJButtonNoText());

        toolBar.addSeparator();

        librarySelector = new LibrarySelector(library, shapeFactory);
        bar.add(librarySelector.buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
        InfoDialog.getInstance().addToFrame(this, MESSAGE);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(parent);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private void createAndStartModel(boolean runClock) {
        ModelBuilder mb = null;
        try {
            if (modelDescription != null)
                modelDescription.highLightOff();

            circuitComponent.setModeAndReset(CircuitComponent.Mode.running);

            mb = new ModelBuilder(circuitComponent.getCircuit())
                    .setDisableClock(!runClock)
                    .setEnableTrace(traceEnable.isSelected(), Main.this);

            model = mb.build(library);
            modelDescription = mb.getModelDescription();
            modelDescription.connectToGui(circuitComponent);

            model.init();
        } catch (NodeException e) {
            if (mb.getModelDescription() != null) {
                modelDescription = mb.getModelDescription();
                if (e.getNodes() != null)
                    mb.getModelDescription().highLight(e.getNodes());
                else
                    mb.getModelDescription().highLight(e.getValues());

                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).setComponent(Main.this));
            circuitComponent.setModeAndReset(CircuitComponent.Mode.part);
        } catch (PinException e) {
            if (mb.getModelDescription() != null) {
                modelDescription = mb.getModelDescription();
                mb.getModelDescription().highLight(e.getVisualElement());
                if (e.getNet() != null)
                    e.getNet().setHighLight(true);
                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).setComponent(Main.this));
            circuitComponent.setModeAndReset(CircuitComponent.Mode.part);
        }
    }

    private static JFileChooser getjFileChooser(File filename) {
        JFileChooser fileChooser = new JFileChooser(filename == null ? null : filename.getParentFile());
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Circuit", "dig"));
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
            setFilename(filename, toPrefs);
        } catch (Exception e) {
            circuitComponent.setCircuit(new Circuit());
            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e).show(this);
        }
    }

    private void saveFile(File filename) {
        if (!filename.getName().endsWith(".dig"))
            filename = new File(filename.getPath() + ".dig");

        try {
            circuitComponent.getCircuit().save(filename);
            if (savedListener != null)
                savedListener.saved(filename);
            setFilename(filename, false);
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

    private class ModeAction extends ToolTipAction {
        private final CircuitComponent.Mode mode;

        public ModeAction(String name, Icon icon, CircuitComponent.Mode mode) {
            super(name, icon);
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelDescription != null)
                modelDescription.highLightOff();
            circuitComponent.setModeAndReset(mode);
            doStep.setEnabled(false);
            if (model != null)
                model.close();
            model = null;
            modelDescription = null;
        }
    }

    private class FullStepObserver implements Observer {
        private final Model model;

        public FullStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            try {
                model.doStep();
            } catch (Exception e) {
                SwingUtilities.invokeLater(
                        new ErrorMessage(Lang.get("msg_errorCalculatingStep")).addCause(e).setComponent(Main.this)
                );
            }
        }
    }

    private class MicroStepObserver implements Observer {
        private final Model model;

        public MicroStepObserver(Model model) {
            this.model = model;
        }

        @Override
        public void hasChanged() {
            modelDescription.highLight(model.nodesToUpdate());
            circuitComponent.repaint();
            doStep.setEnabled(model.needsUpdate());
        }
    }
}
