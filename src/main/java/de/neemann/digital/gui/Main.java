package de.neemann.digital.gui;

import de.neemann.digital.core.PartFactory;
import de.neemann.digital.core.arithmetic.Add;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import javax.swing.*;
import java.awt.*;

/**
 * @author hneemann
 */
public class Main extends JFrame {
    private final CircuitComponent cr;

    public Main() {
        super("Digital");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cr = new CircuitComponent(createDemoCircuit());
        getContentPane().add(cr);

        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private Circuit createDemoCircuit() {
        Circuit cr = new Circuit();
        PartFactory factory = And.createFactory(16, 4);
        cr.add(new VisualPart(new GenericShape("&", factory), factory).setPos(new Vector(10, 10)));
        factory = Add.createFactory(16);
        cr.add(new VisualPart(new GenericShape("+", factory), factory).setPos(new Vector(60, 10)));
        return cr;
    }
}
