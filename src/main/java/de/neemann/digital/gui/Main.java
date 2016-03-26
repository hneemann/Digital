package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SpeedTest;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.elements.PinOrder;
import de.neemann.digital.gui.draw.library.ElementLibrary;
import de.neemann.digital.gui.draw.model.ModelBuilder;
import de.neemann.digital.gui.draw.model.ModelDescription;
import de.neemann.digital.gui.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;
import de.process.utils.gui.*;

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
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private final ElementLibrary library = ShapeFactory.getInstance().setLibrary(new ElementLibrary());
    private final ToolTipAction doStep;
    private final JCheckBoxMenuItem traceEnable;
    private final JCheckBoxMenuItem runClock;
    private final LibrarySelector librarySelector;
    private File lastFilename;
    private File filename;
    private Model model;
    private ModelDescription modelDescription;

    public Main() {
        super(Lang.get("digital"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);


        Circuit cr = new Circuit();
        circuitComponent = new CircuitComponent(cr, library);
        String name = prefs.get("name", null);
        if (name != null) {
            SwingUtilities.invokeLater(() -> loadFile(new File(name)));
        }

        getContentPane().add(circuitComponent);

        addWindowListener(new ClosingWindowListener(this, this));

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();


        ToolTipAction newFile = new ToolTipAction(Lang.get("menu_new")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    setFilename(null);
                    circuitComponent.setCircuit(new Circuit());
                }
            }
        };

        ToolTipAction open = new ToolTipAction(Lang.get("menu_open")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    JFileChooser fc = getjFileChooser(lastFilename);
                    if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                        loadFile(fc.getSelectedFile());
                    }
                }
            }
        };

        ToolTipAction saveas = new ToolTipAction(Lang.get("menu_saveAs")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser(lastFilename);
                if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    saveFile(fc.getSelectedFile());
                }
            }
        };

        save = new ToolTipAction(Lang.get("menu_save")) {
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
                } catch (NodeException e1) {
                    SwingUtilities.invokeLater(
                            new ErrorMessage("Error").addCause(e1).setComponent(Main.this)
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
                            .setBindWiresToGui(false)
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
        toolBar.add(partsMode.createJButtonNoText());
        toolBar.add(wireMode.createJButtonNoText());
        toolBar.add(selectionMode.createJButtonNoText());
        toolBar.add(runModel.createJButtonNoText());
        toolBar.add(runModelMicro.createJButtonNoText());
        toolBar.add(doStep.createJButtonNoText());

        toolBar.addSeparator();

        librarySelector = new LibrarySelector(library);
        bar.add(librarySelector.buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
        InfoDialog.getInstance().addToFrame(this, MESSAGE);
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
                    .setBindWiresToGui(true)
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

    public static JFileChooser getjFileChooser(File filename) {
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

    private void loadFile(File filename) {
        try {
            librarySelector.setFilePath(filename.getParentFile());
            Circuit circ = Circuit.loadCircuit(filename);
            circuitComponent.setCircuit(circ);
            setFilename(filename);
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
            setFilename(filename);
        } catch (IOException e) {
            new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e).show();
        }
    }

    private void setFilename(File filename) {
        this.filename = filename;
        if (filename != null) {
            librarySelector.setLastFile(filename);
            this.lastFilename = filename;
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
            } catch (NodeException e) {
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
