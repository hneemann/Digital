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
import de.process.utils.gui.ClosingWindowListener;
import de.process.utils.gui.ErrorMessage;
import de.process.utils.gui.InfoDialog;
import de.process.utils.gui.ToolTipAction;

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
    private static final String MESSAGE = "Digital\n\nA simple simulator for digital circuits.\nWritten bei H.Neemann in 2016";
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
        super("Digital");
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


        ToolTipAction newFile = new ToolTipAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClosingWindowListener.checkForSave(Main.this, Main.this)) {
                    setFilename(null);
                    circuitComponent.setCircuit(new Circuit());
                }
            }
        };

        ToolTipAction open = new ToolTipAction("Open") {
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

        ToolTipAction saveas = new ToolTipAction("Save As") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser(lastFilename);
                if (fc.showSaveDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    saveFile(fc.getSelectedFile());
                }
            }
        };

        save = new ToolTipAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filename == null)
                    saveas.actionPerformed(e);
                else
                    saveFile(filename);
            }
        };

        JMenu file = new JMenu("File");
        bar.add(file);
        file.add(newFile);
        file.add(open);
        file.add(save);
        file.add(saveas);

        JMenu edit = new JMenu("Edit");
        bar.add(edit);

        ToolTipAction wireMode = new ModeAction("Wire", CircuitComponent.Mode.wire).setToolTip("Edits wires");
        ToolTipAction partsMode = new ModeAction("Parts", CircuitComponent.Mode.part).setToolTip("Moves Parts");
        ToolTipAction selectionMode = new ModeAction("Select", CircuitComponent.Mode.select).setToolTip("Selects circuit sections");

        ToolTipAction orderInputs = new ToolTipAction("Order Inputs") {
            @Override
            public void actionPerformed(ActionEvent e) {
                PinOrder o = new PinOrder(circuitComponent.getCircuit(), "In");
                new ElementOrderer<>(Main.this, "Input Order", o).setVisible(true);
            }
        }.setToolTip("Order inputs for usage as nested model.");

        ToolTipAction orderOutputs = new ToolTipAction("Order Outputs") {
            @Override
            public void actionPerformed(ActionEvent e) {
                PinOrder o = new PinOrder(circuitComponent.getCircuit(), "Out");
                new ElementOrderer<>(Main.this, "Output Order", o).setVisible(true);
            }
        }.setToolTip("Order outputs for usage as nested model.");


        edit.add(partsMode.createJMenuItem());
        edit.add(wireMode.createJMenuItem());
        edit.add(selectionMode.createJMenuItem());
        edit.add(orderInputs.createJMenuItem());
        edit.add(orderOutputs.createJMenuItem());


        JMenu run = new JMenu("Run");
        bar.add(run);

        doStep = new ToolTipAction("Step") {
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
        };

        ToolTipAction runModel = new ToolTipAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndStartModel();
                circuitComponent.setManualChangeObserver(new FullStepObserver(model));
            }
        }.setToolTip("Runs the Model");

        ToolTipAction runModelMicro = new ToolTipAction("Micro") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndStartModel();
                circuitComponent.setManualChangeObserver(new MicroStepObserver(model));
            }
        }.setToolTip("Runs the Model in Micro Stepping Mode");

        ToolTipAction speedTest = new ToolTipAction("SpeedTest") {
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
        }.setToolTip("Performs a speed test by calculating the max. clock frequency.");

        traceEnable = new JCheckBoxMenuItem("Trace");
        runClock = new JCheckBoxMenuItem("Run Clock", true);

        run.add(runModel.createJMenuItem());
        run.add(runModelMicro.createJMenuItem());
        run.add(doStep.createJMenuItem());
        run.add(speedTest.createJMenuItem());
        run.add(traceEnable);
        run.add(runClock);
        doStep.setEnabled(false);

        JToolBar toolBar = new JToolBar();
        toolBar.add(partsMode.createJButton());
        toolBar.add(wireMode.createJButton());
        toolBar.add(selectionMode.createJButton());
        toolBar.add(runModel.createJButton());
        toolBar.add(runModelMicro.createJButton());
        toolBar.add(doStep.createJButton());

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

    private void createAndStartModel() {
        try {
            if (modelDescription != null)
                modelDescription.highLightOff();

            circuitComponent.setModeAndReset(CircuitComponent.Mode.running);

            ModelBuilder mb = new ModelBuilder(circuitComponent.getCircuit())
                    .setBindWiresToGui(true)
                    .setDisableClock(!runClock.isSelected())
                    .setEnableTrace(traceEnable.isSelected(), Main.this);

            model = mb.build(library);
            modelDescription = mb.getModelDescription();
            modelDescription.connectToGui(circuitComponent);

            model.init();
        } catch (NodeException e) {
            if (modelDescription != null) {
                if (e.getNodes() != null)
                    modelDescription.highLight(e.getNodes());
                else
                    modelDescription.highLight(e.getValues());

                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage("error creating model").addCause(e).setComponent(Main.this));
            circuitComponent.setModeAndReset(CircuitComponent.Mode.part);
        } catch (PinException e) {
            if (modelDescription != null) {
                modelDescription.highLight(e.getVisualElement());
                if (e.getNet() != null)
                    e.getNet().setHighLight(true);
                circuitComponent.repaint();
            }
            SwingUtilities.invokeLater(new ErrorMessage("error creating model").addCause(e).setComponent(Main.this));
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
            new ErrorMessage("error reading a file").addCause(e).show(this);
        }
    }

    private void saveFile(File filename) {
        if (!filename.getName().endsWith(".dig"))
            filename = new File(filename.getPath() + ".dig");

        try {
            circuitComponent.getCircuit().save(filename);
            setFilename(filename);
        } catch (IOException e) {
            new ErrorMessage("error writing a file").addCause(e).show();
        }
    }

    private void setFilename(File filename) {
        this.filename = filename;
        if (filename != null) {
            librarySelector.setLastFile(filename);
            this.lastFilename = filename;
            prefs.put("name", filename.getPath());
            setTitle(filename + " - Digital");
        } else
            setTitle("Digital");
    }

    private class ModeAction extends ToolTipAction {
        private final CircuitComponent.Mode mode;

        public ModeAction(String name, CircuitComponent.Mode mode) {
            super(name);
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
                        new ErrorMessage("Error").addCause(e).setComponent(Main.this)
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
