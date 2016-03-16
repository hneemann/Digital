package de.neemann.digital.gui;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.core.PartFactory;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * @author hneemann
 */
public class Main extends JFrame {
    private final CircuitComponent circuitComponent;
    private final Circuit cr;

    public Main() {
        super("Digital");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cr = new Circuit();
        circuitComponent = new CircuitComponent(cr);
        getContentPane().add(circuitComponent);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);

        JMenuBar bar = new JMenuBar();

        JMenu parts = new JMenu("Parts");
        bar.add(parts);
        parts.add(createSimpleMenu("AND", "&", () -> new And(1), false));
        parts.add(createSimpleMenu("OR", "\u22651", () -> new Or(1), false));
        parts.add(createSimpleMenu("NAND", "&", () -> new NAnd(1), true));
        parts.add(createSimpleMenu("NOR", "\u22651", () -> new NOr(1), true));

        setJMenuBar(bar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private JMenu createSimpleMenu(String name, String s, PartFactory creator, boolean invers) {
        JMenu m = new JMenu(name);
        for (int i = 2; i < 16; i++) {
            m.add(new JMenuItem(new InsertAbstractAction(i, s, creator, invers)));
        }
        return m;
    }

    private class InsertAbstractAction extends AbstractAction {
        private final int i;
        private final String s;
        private final boolean invers;
        private PartFactory creator;

        public InsertAbstractAction(int i, String s, PartFactory creator, boolean invers) {
            super(Integer.toString(i));
            this.i = i;
            this.s = s;
            this.creator = creator;
            this.invers = invers;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PartDescription factory = new FanIn.FanInDescription(i, creator);
            cr.add(new VisualPart(new GenericShape(s, factory).invert(invers), factory).setPos(new Vector(10, 10)));
        }
    }

}
