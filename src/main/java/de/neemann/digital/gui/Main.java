package de.neemann.digital.gui;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.Part;
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
        cr.add(new Part(new GenericShape("&", 2, 1)).setPos(new Vector(10, 10)));
        return cr;
    }
}
