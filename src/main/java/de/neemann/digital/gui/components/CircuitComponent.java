package de.neemann.digital.gui.components;

import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.parts.Circuit;
import de.neemann.digital.gui.draw.parts.Moveable;
import de.neemann.digital.gui.draw.parts.VisualPart;
import de.neemann.digital.gui.draw.parts.Wire;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent {

    private static final String delAction = "myDelAction";
    private final Circuit circuit;
    private Mouse listener;

    public CircuitComponent(Circuit circuit) {
        this.circuit = circuit;
        setMode(Mode.part);

        KeyStroke delKey = KeyStroke.getKeyStroke("DELETE");
        getInputMap().put(delKey, delAction);
        getActionMap().put(delAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("write");
                if (listener instanceof SelectMouseListener) {
                    SelectMouseListener mml = (SelectMouseListener) listener;
                    if (mml.corner1 != null && mml.corner2 != null) {
                        circuit.delete(Vector.min(mml.corner1, mml.corner2), Vector.max(mml.corner1, mml.corner2));
                        mml.reset();
                        repaint();
                    }
                }
            }
        });

        setFocusable(true);
    }

    public void setMode(Mode mode) {
        if (listener != null) {
            removeMouseListener(listener);
            removeMouseMotionListener(listener);
        }
        switch (mode) {
            case part:
                listener = new PartMouseListener();
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                break;
            case wire:
                listener = new WireMouseListener();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case select:
                listener = new SelectMouseListener();
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
                break;
        }
        addMouseMotionListener(listener);
        addMouseListener(listener);
        requestFocusInWindow();
        repaint();
    }

    public void setPartToDrag(VisualPart part) {
        setMode(Mode.part);
        ((PartMouseListener) listener).setPartToInsert(part);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        GraphicSwing gr = new GraphicSwing((Graphics2D) g);
        circuit.drawTo(gr);

        listener.drawTo(gr);
    }

    private Vector raster(Vector pos) {
        return new Vector(((pos.x + GenericShape.SIZE2) / GenericShape.SIZE) * GenericShape.SIZE,
                ((pos.y + GenericShape.SIZE2) / GenericShape.SIZE) * GenericShape.SIZE);
    }

    public enum Mode {part, wire, select}

    private interface Mouse extends MouseMotionListener, MouseListener {
        void drawTo(Graphic gr);
    }

    private class WireMouseListener implements Mouse {

        private Wire wire;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (wire != null) {
                    circuit.add(wire);
                    repaint();
                }
                Vector startPos = raster(new Vector(e.getX(), e.getY()));
                wire = new Wire(startPos, startPos);
                repaint();
            } else {
                wire = null;
                repaint();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (wire != null) {
                wire.setP2(raster(new Vector(e.getX(), e.getY())));
                repaint();
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            if (wire != null)
                wire.drawTo(gr);
        }
    }

    private class PartMouseListener implements Mouse {

        private VisualPart partToInsert;
        private boolean autoPick = false;
        private Vector delta;
        private boolean insert;

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (partToInsert != null) {
                Vector pos = new Vector(e.getX(), e.getY());
                partToInsert.setPos(raster(pos.add(delta)));
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (partToInsert == null) {
                Vector pos = new Vector(e.getX(), e.getY());
                insert = false;
                for (VisualPart vp : circuit.getParts())
                    if (vp.matches(pos)) {
                        partToInsert = vp;
                        delta = partToInsert.getPos().sub(pos);
                        break;
                    }
            } else {
                partToInsert.setPos(raster(partToInsert.getPos()));
                if (insert)
                    circuit.add(partToInsert);
                repaint();
                partToInsert = null;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (autoPick && partToInsert != null) {
                Vector pos = new Vector(e.getX(), e.getY());
                delta = partToInsert.getMinMax().getMin().sub(partToInsert.getMinMax().getMax());
                partToInsert.setPos(raster(pos.add(delta)));
                autoPick = false;
                repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        public void setPartToInsert(VisualPart partToInsert) {
            this.partToInsert = partToInsert;
            insert = true;
            autoPick = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            if (partToInsert != null)
                partToInsert.drawTo(gr);
        }
    }

    private class SelectMouseListener implements Mouse {
        private Vector corner1;
        private Vector corner2;
        private ArrayList<Moveable> elementsToMove;
        private Vector lastPos;

        @Override
        public void mouseClicked(MouseEvent e) {
            reset();
            repaint();
        }

        private void reset() {
            corner1 = null;
            corner2 = null;
            elementsToMove = null;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (corner1 == null) {
                corner1 = new Vector(e.getX(), e.getY());
            } else {
                elementsToMove = circuit.getElementsMatching(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                lastPos = new Vector(e.getX(), e.getY());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (elementsToMove != null) {
                Vector pos = new Vector(e.getX(), e.getY());
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {

                    for (Moveable m : elementsToMove)
                        m.move(delta);

                    corner1.move(delta);
                    corner2.move(delta);
                    repaint();

                    lastPos = lastPos.add(delta);
                }
            } else {
                corner2 = new Vector(e.getX(), e.getY());
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public void drawTo(Graphic gr) {
            if (corner1 != null && corner2 != null) {
                Polygon p = new Polygon(true)
                        .add(corner1)
                        .add(new Vector(corner1.x, corner2.y))
                        .add(corner2)
                        .add(new Vector(corner2.x, corner1.y));
                gr.drawPolygon(p, Style.THIN);
            }
        }
    }

}
