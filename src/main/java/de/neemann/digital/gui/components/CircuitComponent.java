package de.neemann.digital.gui.components;

import de.neemann.digital.gui.draw.graphics.GraphicSwing;
import de.neemann.digital.gui.draw.parts.Circuit;

import javax.swing.*;
import java.awt.*;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent {

    private final Circuit circuit;

    public CircuitComponent(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        GraphicSwing gr = new GraphicSwing((Graphics2D) g);
        circuit.drawTo(gr);
    }
}
