package de.neemann.digital.gui.components;

import de.neemann.digital.gui.draw.graphics.GraphicSwing;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent {

    private final Circuit circuit;

    public CircuitComponent(Circuit circuit) {
        this.circuit = circuit;

        MyMouseMotionListener l = new MyMouseMotionListener();
        addMouseMotionListener(l);
        addMouseListener(l);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        GraphicSwing gr = new GraphicSwing((Graphics2D) g);
        circuit.drawTo(gr);
    }

    private Vector raster(Vector pos) {
        return new Vector(((pos.x + GenericShape.SIZE2) / GenericShape.SIZE) * GenericShape.SIZE,
                ((pos.y + GenericShape.SIZE2) / GenericShape.SIZE) * GenericShape.SIZE);
    }

    private class MyMouseMotionListener implements MouseMotionListener, MouseListener {

        private Vector lastPos;
        private VisualPart partToDrag;

        @Override
        public void mouseDragged(MouseEvent e) {
            Vector pos = new Vector(e.getX(), e.getY());

            if (partToDrag != null) {
                partToDrag.move(pos.sub(lastPos));
                repaint();
            }

            lastPos = pos;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastPos = new Vector(e.getX(), e.getY());
            for (VisualPart vp : circuit.getParts())
                if (vp.matches(lastPos)) {
                    partToDrag = vp;
                    break;
                }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            partToDrag.setPos(raster(partToDrag.getPos()));
            repaint();
            partToDrag = null;
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

}
