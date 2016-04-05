package de.neemann.digital.gui.components;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Moveable;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.GenericShape;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.LibrarySelector;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.SavedListener;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.IconCreator;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent {
    private static final Icon iconDelete = IconCreator.create("Delete24.gif");

    private static final String delAction = "myDelAction";
    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final HashSet<Drawable> highLighted;
    private final DelAction deleteAction;
    private Circuit circuit;
    private Mouse listener;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;

    public CircuitComponent(Circuit aCircuit, ElementLibrary library, ShapeFactory shapeFactory) {
        this.library = library;
        this.shapeFactory = shapeFactory;
        highLighted = new HashSet<>();
        deleteAction = new DelAction();
        setCircuit(aCircuit);

        KeyStroke delKey = KeyStroke.getKeyStroke("DELETE");
        getInputMap().put(delKey, delAction);
        getActionMap().put(delAction, deleteAction);

        setFocusable(true);

        addMouseWheelListener(e -> {
            Vector pos = getPosVector(e);
            double f = Math.pow(0.9, e.getWheelRotation());
            transform.translate(pos.x, pos.y);
            transform.scale(f, f);
            transform.translate(-pos.x, -pos.y);
            repaint();
        });
    }

    public ToolTipAction getDeleteAction() {
        return deleteAction;
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
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
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
        deleteAction.setEnabled(false);
        repaint();
    }

    public Collection<Drawable> getHighLighted() {
        return highLighted;
    }

    public <T extends Drawable> void addHighLighted(T drawable) {
        highLighted.add(drawable);
    }

    public void addHighLighted(Collection<? extends Drawable> drawables) {
        highLighted.addAll(drawables);
    }

    public void addHighLightedWires(ObservableValue[] values) {
        HashSet<ObservableValue> ov = new HashSet<>();
        ov.addAll(Arrays.asList(values));
        for (Wire w : circuit.getWires())
            if (ov.contains(w.getValue()))
                addHighLighted(w);
    }

    public void removeHighLighted() {
        highLighted.clear();
    }

    public void setPartToDrag(VisualElement part) {
        if (listener instanceof PartMouseListener)
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
        circuit.drawTo(gr, highLighted);

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
                    Vector startPos = raster(getPosVector(e));
                    if (circuit.isPinPos(startPos))
                        wire = null;
                    else
                        wire = new Wire(startPos, startPos);
                } else {
                    Vector startPos = raster(getPosVector(e));
                    wire = new Wire(startPos, startPos);
                }
                repaint();
            } else {
                if (wire != null) {
                    wire = null;
                    repaint();
                } else
                    editAttributes(e);
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
                wire.drawTo(gr, false);
        }
    }

    private class PartMouseListener extends Mouse {

        private VisualElement partToInsert;
        private Wire wire;
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
            if (wire != null) {
                wire.setP2(raster(getPosVector(e)));
                repaint();
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (partToInsert == null) {
                    insert = false;
                    if (wire == null) {
                        Vector pos = getPosVector(e);
                        VisualElement vp = circuit.getElementAt(pos);
                        if (vp != null) {
                            Vector startPos = raster(pos);
                            if (circuit.isPinPos(startPos)) {
                                wire = new Wire(startPos, startPos);
                            } else {
                                partToInsert = vp;
                                delta = partToInsert.getPos().sub(pos);
                            }
                            repaint();
                        } else {
                            Vector startPos = raster(pos);
                            wire = new Wire(startPos, startPos);
                        }
                    } else {
                        circuit.add(wire);
                        Vector startPos = raster(getPosVector(e));
                        if (circuit.isPinPos(startPos))
                            wire = null;
                        else
                            wire = new Wire(startPos, startPos);
                    }
                } else {
                    partToInsert.setPos(raster(partToInsert.getPos()));
                    if (insert)
                        circuit.add(partToInsert);
                    circuit.modified();
                    repaint();
                    partToInsert = null;
                }
                deleteAction.setEnabled(partToInsert != null);
            } else {
                if (wire != null) {
                    wire = null;
                    repaint();
                } else
                    editAttributes(e);
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
                deleteAction.setEnabled(true);
                repaint();
            }
        }

        public void setPartToInsert(VisualElement partToInsert) {
            this.partToInsert = partToInsert;
            insert = true;
            autoPick = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            if (partToInsert != null && !autoPick)
                partToInsert.drawTo(gr, true);
            if (wire != null)
                wire.drawTo(gr, false);
        }
    }

    private boolean editAttributes(MouseEvent e) {
        VisualElement vp = circuit.getElementAt(getPosVector(e));
        if (vp != null) {
            String name = vp.getElementName();
            ElementTypeDescription elementType = library.getElementType(name);
            if (elementType instanceof LibrarySelector.ElementTypeDescriptionCustom) {
                new Main(this, ((LibrarySelector.ElementTypeDescriptionCustom) elementType).getFile(), new SavedListener() {
                    @Override
                    public void saved(File filename) {
                        library.removeElement(filename.getName());
                        circuit.clearState();
                        repaint();
                    }
                }).setVisible(true);
            } else {
                ArrayList<AttributeKey> list = elementType.getAttributeList();
                if (list.size() > 0) {
                    Point p = new Point(e.getX(), e.getY());
                    SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                    if (new AttributeDialog(this, p, list, vp.getElementAttributes()).showDialog()) {
                        circuit.modified();
                        repaint();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private enum State {COPY, MOVE}

    private class SelectMouseListener extends Mouse {
        private Vector corner1;
        private Vector corner2;
        private ArrayList<Moveable> elements;
        private Vector lastPos;
        private State state;
        private Vector copyStartPosition;
        private boolean wasRealyDragged;

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1)
                reset();
            else
                editAttributes(e);
        }

        private void reset() {
            corner1 = null;
            corner2 = null;
            elements = null;
            deleteAction.setEnabled(false);
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (corner1 == null) {
                corner1 = getPosVector(e);
                wasRealyDragged = false;
            } else {
                if (corner2 != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        elements = circuit.getElementsToMove(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                        state = State.MOVE;
                    } else {
                        elements = circuit.getElementsToCopy(Vector.min(corner1, corner2), Vector.max(corner1, corner2), shapeFactory);
                        copyStartPosition = raster(getPosVector(e));
                        state = State.COPY;
                    }
                }
                lastPos = getPosVector(e);
            }
            deleteAction.setEnabled(corner1 != null && corner2 != null);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (elements != null && state == State.COPY && copyStartPosition != null && !copyStartPosition.equals(raster(getPosVector(e)))) {
                for (Moveable m : elements) {
                    if (m instanceof Wire)
                        circuit.add((Wire) m);
                    if (m instanceof VisualElement)
                        circuit.add((VisualElement) m);
                }
                copyStartPosition = null;
            }
            if (wasRealyDragged)
                reset();
            else
                deleteAction.setEnabled(corner1 != null && corner2 != null);
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
                Vector p1 = new Vector(corner1.x, corner2.y);
                Vector p2 = new Vector(corner2.x, corner1.y);
                gr.drawLine(corner1, p1, Style.DASH);
                gr.drawLine(p1, corner2, Style.DASH);
                gr.drawLine(corner2, p2, Style.DASH);
                gr.drawLine(p2, corner1, Style.DASH);
            }
            if (state == State.COPY && elements != null)
                for (Moveable m : elements)
                    if (m instanceof Drawable)
                        ((Drawable) m).drawTo(gr, true);
        }
    }

    private class RunningMouseListener extends Mouse {
        @Override
        public void drawTo(Graphic gr) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            VisualElement ve = circuit.getElementAt(getPosVector(e));
            if (ve != null) {
                Point p = new Point(e.getX(), e.getY());
                SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                ve.clicked(CircuitComponent.this, p);
                if (manualChangeObserver != null)
                    manualChangeObserver.hasChanged();
            }
        }
    }

    private class DelAction extends ToolTipAction {

        DelAction() {
            super(Lang.get("menu_delete"), iconDelete);
            setToolTip(Lang.get("menu_delete_tt"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (listener instanceof SelectMouseListener) {
                SelectMouseListener mml = (SelectMouseListener) listener;
                if (mml.corner1 != null && mml.corner2 != null) {
                    circuit.delete(Vector.min(mml.corner1, mml.corner2), Vector.max(mml.corner1, mml.corner2));
                    mml.reset();
                    repaint();
                }
            } else if (listener instanceof PartMouseListener) {
                PartMouseListener pml = (PartMouseListener) listener;
                if (!pml.insert) {
                    circuit.delete(pml.partToInsert);
                }
                pml.partToInsert = null;
                deleteAction.setEnabled(false);
                repaint();
            }
        }
    }
}
