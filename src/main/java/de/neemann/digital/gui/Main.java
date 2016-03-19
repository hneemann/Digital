package de.neemann.digital.gui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.model.ModelDescription;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.parts.Wire;
import de.neemann.digital.gui.draw.shapes.ShapeFactory;
import de.process.utils.gui.ClosingWindowListener;
import de.process.utils.gui.ErrorMessage;
import de.process.utils.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * @author hneemann
 */
public class Main extends JFrame implements ClosingWindowListener.ConfirmSave {
    private static final Preferences prefs = Preferences.userRoot().node("dig");
    private final CircuitComponent circuitComponent;
    private final ToolTipAction save;
    private final PartLibrary library = ShapeFactory.INSTANCE.setLibrary(new PartLibrary());
    private final ToolTipAction doStep;
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
            loadFile(new File(name));
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
                setFilename(null);
                circuitComponent.setCircuit(new Circuit());
            }
        };

        ToolTipAction open = new ToolTipAction("Open") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser();
                if (fc.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
                    loadFile(fc.getSelectedFile());
                }
            }
        };

        ToolTipAction saveas = new ToolTipAction("Save As") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = getjFileChooser();
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

        edit.add(partsMode.createJMenuItem());
        edit.add(wireMode.createJMenuItem());
        edit.add(selectionMode.createJMenuItem());


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
                circuitComponent.setModel(model, new FullStepObserver(model));
            }
        }.setToolTip("Runs the Model");

        ToolTipAction runModelMicro = new ToolTipAction("Micro") {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAndStartModel();
                circuitComponent.setModel(model, new MicroStepObserver(model));
            }
        }.setToolTip("Runs the Model in Micro mode");

        run.add(runModel.createJMenuItem());
        run.add(runModelMicro.createJMenuItem());
        run.add(doStep.createJMenuItem());
        doStep.setEnabled(false);

        JToolBar toolBar = new JToolBar();
        toolBar.add(partsMode.createJButton());
        toolBar.add(wireMode.createJButton());
        toolBar.add(selectionMode.createJButton());
        toolBar.add(runModel.createJButton());
        toolBar.add(runModelMicro.createJButton());
        toolBar.add(doStep.createJButton());

        toolBar.addSeparator();

        bar.add(new LibrarySelector(library).buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
    }

    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("visualPart", VisualPart.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        xStream.alias("vector", Vector.class);
        xStream.alias("key", AttributeKey.class);
        xStream.addImplicitCollection(PartAttributes.class, "attributes");
        xStream.aliasAttribute(Vector.class, "x", "x");
        xStream.aliasAttribute(Vector.class, "y", "y");
        return xStream;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private void createAndStartModel() {
        try {
            circuitComponent.setModeAndReset(CircuitComponent.Mode.running);
            modelDescription = new ModelDescription(circuitComponent.getCircuit(), library);
            model = modelDescription.createModel();
            modelDescription.connectToGui(circuitComponent);
            model.init();
        } catch (Exception e1) {
            new ErrorMessage("error creating model").addCause(e1).show(Main.this);
        }
    }

    private JFileChooser getjFileChooser() {
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
        XStream xStream = getxStream();
        try (FileReader in = new FileReader(filename)) {
            circuitComponent.setCircuit((Circuit) xStream.fromXML(in));
            setFilename(filename);
        } catch (Exception e) {
            new ErrorMessage("error writing a file").addCause(e).show();
        }
    }

    private void setFilename(File filename) {
        this.filename = filename;
        if (filename != null) {
            prefs.put("name", filename.getPath());
            setTitle(filename + " - Digital");
        } else
            setTitle("Digital");
    }

    private void saveFile(File filename) {
        if (!filename.getName().endsWith(".dig"))
            filename = new File(filename.getPath() + ".dig");

        XStream xStream = getxStream();
        try (FileWriter out = new FileWriter(filename)) {
            xStream.marshal(circuitComponent.getCircuit(), new PrettyPrintWriter(out));
            setFilename(filename);
            circuitComponent.getCircuit().saved();
        } catch (IOException e) {
            new ErrorMessage("error writing a file").addCause(e).show();
        }
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
                modelDescription.highLight(null);
            circuitComponent.setModeAndReset(mode);
            doStep.setEnabled(false);
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
