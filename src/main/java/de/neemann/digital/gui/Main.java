package de.neemann.digital.gui;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.model.ModelDescription;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.parts.Wire;
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
    private final PartLibrary library = new PartLibrary();
    private File filename;

    public Main() {
        super("Digital");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);


        Circuit cr = new Circuit();
        circuitComponent = new CircuitComponent(cr);
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

        JCheckBox microStep = new JCheckBox("micro");

        ToolTipAction runModel = new ToolTipAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    circuitComponent.setMode(CircuitComponent.Mode.running);
                    ModelDescription m = new ModelDescription(circuitComponent.getCircuit(), library);
                    Model model = m.createModel(circuitComponent);
                    if (microStep.isSelected()) {
                        model.setListener(new Listener() {
                            @Override
                            public void needsUpdate() {
                                circuitComponent.paintImmediately(circuitComponent.getVisibleRect());
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        });
                    }
                    model.init(true);
                } catch (Exception e1) {
                    new ErrorMessage("error creating model").addCause(e1).show(Main.this);
                }
            }
        }.setToolTip("Runs the Model");
        run.add(runModel.createJMenuItem());



        JToolBar toolBar = new JToolBar();
        toolBar.add(partsMode.createJButton());
        toolBar.add(wireMode.createJButton());
        toolBar.add(selectionMode.createJButton());
        toolBar.add(runModel.createJButton());
        toolBar.add(microStep);

        toolBar.addSeparator();

        bar.add(new LibrarySelector(library).buildMenu(new InsertHistory(toolBar), circuitComponent));

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("visualPart", VisualPart.class);
        xStream.alias("wire", Wire.class);
        xStream.alias("circuit", Circuit.class);
        return xStream;
    }

    private JFileChooser getjFileChooser() {
        JFileChooser fileChooser = new JFileChooser(filename == null ? null : filename.getParentFile());
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Circuit", "dig"));
        return fileChooser;
    }

    @Override
    public boolean isStateChanged() {
        return false;
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
            circuitComponent.setMode(mode);
        }
    }
}
