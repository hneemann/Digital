package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.PartDescription;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.model.ModelDescription;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.process.utils.gui.ErrorMessage;
import de.process.utils.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class Main extends JFrame {
    private final CircuitComponent circuitComponent;
    private final InsertHistory insertHistory;

    public Main() {
        super("Digital");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Circuit cr = new Circuit();
        circuitComponent = new CircuitComponent(cr);
        getContentPane().add(circuitComponent);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();

        JMenu parts = new JMenu("Parts");
        bar.add(parts);
        parts.add(createSimpleMenu("AND", inputs -> And.createFactory(1, inputs)));
        parts.add(createSimpleMenu("OR", inputs -> Or.createFactory(1, inputs)));
        parts.add(createSimpleMenu("NAND", inputs -> NAnd.createFactory(1, inputs)));
        parts.add(createSimpleMenu("NOR", inputs -> NOr.createFactory(1, inputs)));
        parts.add(new InsertAction("Not", Not.createFactory(1)));
        parts.add(new InsertAction("In", In.createFactory(1)));
        parts.add(new InsertAction("Out", Out.createFactory(1)));

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

        ToolTipAction runModel = new ToolTipAction("Run") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ModelDescription m = new ModelDescription(cr);
                    Model model = m.create(circuitComponent);
                    model.init(true);
                    circuitComponent.setMode(CircuitComponent.Mode.running);
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

        toolBar.addSeparator();
        insertHistory = new InsertHistory(toolBar);

        getContentPane().add(toolBar, BorderLayout.NORTH);

        setJMenuBar(bar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private JMenu createSimpleMenu(String name, DescriptionFactory factory) {
        JMenu m = new JMenu(name);
        for (int i = 2; i <= 16; i++) {
            m.add(new JMenuItem(new InsertAction(name + " (" + Integer.toString(i) + ")", factory.create(i))));
        }
        return m;
    }

    private interface DescriptionFactory {
        PartDescription create(int inputs);
    }

    private class InsertAction extends ToolTipAction {
        private final PartDescription partDescription;

        public InsertAction(String name, PartDescription partDescription) {
            super(name, new VisualPart(partDescription).createIcon(60));
            this.partDescription = partDescription;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            VisualPart visualPart = new VisualPart(partDescription).setPos(new Vector(10, 10));
            circuitComponent.setPartToDrag(visualPart);
            insertHistory.add(this);
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
