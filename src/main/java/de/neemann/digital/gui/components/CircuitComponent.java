package de.neemann.digital.gui.components;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.Moveable;
import de.neemann.digital.gui.draw.elements.VisualElement;
import de.neemann.digital.gui.draw.elements.Wire;
import de.neemann.digital.gui.draw.graphics.*;
import de.neemann.digital.gui.draw.graphics.Polygon;
import de.neemann.digital.gui.draw.library.PartLibrary;
import de.neemann.digital.gui.draw.shapes.Drawable;
import de.neemann.digital.gui.draw.shapes.GenericShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent implements Observer {

    private static final String delAction = "myDelAction";
    private final PartLibrary library;
    private Circuit circuit;
    private Mouse listener;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;

    public CircuitComponent(Circuit aCircuit, PartLibrary library) {
        this.library = library;
        setCircuit(aCircuit);

        KeyStroke delKey = KeyStroke.getKeyStroke("DELETE");
        getInputMap().put(delKey, delAction);
        getActionMap().put(delAction, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Vector pos = getPosVector(e);
                double f = Math.pow(0.9, e.getWheelRotation());
                transform.translate(pos.x, pos.y);
                transform.scale(f, f);
                transform.translate(-pos.x, -pos.y);
                repaint();
            }
        });
    }

    public void setManualChangeObserver(Observer callOnManualChange) {
        this.manualChangeObserver = callOnManualChange;
    }

    public void setModeAndReset(Mode mode) {
        if (listener != null) {
            removeMouseListener(listener);
            removeMouseMotionListener(listener);
        }
        switch (mode) {
            case part:
                listener = new PartMouseListener();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                break;
            case wire:
                listener = new WireMouseListener();
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case select:
                listener = new SelectMouseListener();
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
                break;
            case running:
                listener = new RunningMouseListener();
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                break;
        }
        addMouseMotionListener(listener);
        addMouseListener(listener);
        requestFocusInWindow();
        circuit.clearState();
        repaint();
    }

    public void setPartToDrag(VisualElement part) {
        setModeAndReset(Mode.part);
        ((PartMouseListener) listener).setPartToInsert(part);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gr2 = (Graphics2D) g;
        gr2.setColor(Color.WHITE);
        gr2.fillRect(0, 0, getWidth(), getHeight());
        AffineTransform oldTrans = gr2.getTransform();
        gr2.transform(transform);

        GraphicSwing gr = new GraphicSwing(gr2);
        circuit.drawTo(gr);

        listener.drawTo(gr);
        gr2.setTransform(oldTrans);
    }

    private Vector getPosVector(MouseEvent e) {
        try {
            Point2D.Double p = new Point2D.Double();
            transform.inverseTransform(new Point(e.getX(), e.getY()), p);
            return new Vector((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException(e1);
        }
    }

    private Vector raster(Vector pos) {
        return new Vector((int) Math.round((double) pos.x / GenericShape.SIZE) * GenericShape.SIZE,
                (int) Math.round((double) pos.y / GenericShape.SIZE) * GenericShape.SIZE);
    }

    @Override
    public void hasChanged() {
        repaint();
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;

        GraphicMinMax gr = new GraphicMinMax();
        circuit.drawTo(gr);
        if (gr.getMin() != null) {

            Vector delta = gr.getMax().sub(gr.getMin());
            double sx = ((double) getWidth()) / delta.x * 0.8;
            double sy = ((double) getHeight()) / delta.y * 0.95;
            double s = Math.min(sx, sy);

            transform.setToScale(s, s);  // set Scaling

            Vector center = gr.getMin().add(gr.getMax()).div(2);
            transform.translate(-center.x, -center.y);  // move drawing center to (0,0)

            Vector dif = new Vector(getWidth(), getHeight()).div(2);
            transform.translate(dif.x / s, dif.y / s);  // move drawing center to frame center
        } else
            transform = new AffineTransform();

        setModeAndReset(Mode.part);
    }

    public enum Mode {part, wire, running, select}

    private abstract class Mouse extends MouseAdapter implements MouseMotionListener {
        private Vector pos;

        abstract void drawTo(Graphic gr);

        @Override
        public void mousePressed(MouseEvent e) {
            pos = new Vector(e.getX(), e.getY());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Vector newPos = new Vector(e.getX(), e.getY());
            Vector delta = newPos.sub(pos);
            double s = transform.getScaleX();
            transform.translate(delta.x / s, delta.y / s);
            pos = newPos;
            repaint();
        }
    }

    private class WireMouseListener extends Mouse {

        private Wire wire;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (wire != null) {
                    circuit.add(wire);
                    repaint();
                }
                Vector startPos = raster(getPosVector(e));
                wire = new Wire(startPos, startPos);
                repaint();
            } else {
                wire = null;
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (wire != null) {
                wire.setP2(raster(getPosVector(e)));
                repaint();
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            if (wire != null)
                wire.drawTo(gr);
        }
    }

    private class PartMouseListener extends Mouse {

        private VisualElement partToInsert;
        private boolean autoPick = false;
        private Vector delta;
        private boolean insert;

        @Override
        public void mouseMoved(MouseEvent e) {
            if (partToInsert != null) {
                Vector pos = getPosVector(e);
                partToInsert.setPos(raster(pos.add(delta)));
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (partToInsert == null) {
                    Vector pos = getPosVector(e);
                    insert = false;
                    for (VisualElement vp : circuit.getParts())
                        if (vp.matches(pos)) {
                            partToInsert = vp;
                            partToInsert.setHighLight(true);
                            delta = partToInsert.getPos().sub(pos);
                            repaint();
                            break;
                        }
                } else {
                    partToInsert.setPos(raster(partToInsert.getPos()));
                    if (insert)
                        circuit.add(partToInsert);
                    partToInsert.setHighLight(false);
                    circuit.modified();
                    repaint();
                    partToInsert = null;
                }
            } else {
                Vector pos = getPosVector(e);
                for (VisualElement vp : circuit.getParts())
                    if (vp.matches(pos)) {
                        String name = vp.getElementName();
                        ArrayList<AttributeKey> list = library.getPartType(name).getAttributeList();
                        if (list.size() > 0) {
                            Point p = new Point(e.getX(), e.getY());
                            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                            new AttributeDialog(p, list, vp.getElementAttributes()).showDialog();
                            circuit.modified();
                            repaint();
                        }
                    }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (autoPick && partToInsert != null) {
                GraphicMinMax minMax = partToInsert.getMinMax();
                delta = partToInsert.getPos().sub(minMax.getMax());

                Vector pos = getPosVector(e);
                partToInsert.setPos(raster(pos.add(delta)));
                autoPick = false;
                repaint();
            }
        }

        public void setPartToInsert(VisualElement partToInsert) {
            this.partToInsert = partToInsert;
            partToInsert.setHighLight(true);
            insert = true;
            autoPick = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            if (partToInsert != null && !autoPick)
                partToInsert.drawTo(gr);
        }
    }

    private static enum State {COPY, MOVE}

    private class SelectMouseListener extends Mouse {
        private Vector corner1;
        private Vector corner2;
        private ArrayList<Moveable> elements;
        private Vector lastPos;
        private State state;
        private boolean wasRealyDragged;

        @Override
        public void mouseClicked(MouseEvent e) {
            reset();
        }

        private void reset() {
            corner1 = null;
            corner2 = null;
            elements = null;
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (corner1 == null) {
                corner1 = getPosVector(e);
                wasRealyDragged = false;
            } else {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    elements = circuit.getElementsToMove(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                    state = State.MOVE;
                } else {
                    elements = circuit.getElementsToCopy(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                    state = State.COPY;
                }
                lastPos = getPosVector(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (elements != null && state == State.COPY) {
                for (Moveable m : elements) {
                    if (m instanceof Wire)
                        circuit.add((Wire) m);
                    if (m instanceof VisualElement)
                        circuit.add((VisualElement) m);
                }
                reset();
            }
            if (wasRealyDragged)
                reset();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Moveable m : elements)
                        m.move(delta);
                    circuit.modified();

                    corner1.move(delta);
                    corner2.move(delta);
                    repaint();

                    lastPos = lastPos.add(delta);
                    wasRealyDragged = true;
                }
            } else {
                corner2 = getPosVector(e);
                repaint();
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            if (corner1 != null && corner2 != null) {
                Polygon p = new Polygon(true)
                        .add(corner1)
                        .add(new Vector(corner1.x, corner2.y))
                        .add(corner2)
                        .add(new Vector(corner2.x, corner1.y));
                gr.drawPolygon(p, Style.DASH);
            }
            if (state == State.COPY && elements != null)
                for (Moveable m : elements)
                    if (m instanceof Drawable)
                        ((Drawable) m).drawTo(gr);
        }
    }

    private class RunningMouseListener extends Mouse {
        @Override
        public void drawTo(Graphic gr) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            Vector pos = getPosVector(e);
            for (VisualElement vp : circuit.getParts())
                if (vp.matches(pos)) {
                    Point p = new Point(e.getX(), e.getY());
                    SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                    vp.clicked(CircuitComponent.this, p);
                    if (manualChangeObserver != null)
                        manualChangeObserver.hasChanged();
                }
        }
    }
}
