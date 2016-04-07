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
    private static final Icon ICON_DELETE = IconCreator.create("Delete24.gif");
    private static final String DEL_ACTION = "myDelAction";

    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final HashSet<Drawable> highLighted;
    private final ToolTipAction deleteAction;

    private final MouseController mouseNormal;
    private final MouseControllerInsertElement mouseInsertElement;
    private final MouseControllerMoveElement mouseMoveElement;
    private final MouseControllerWire mouseWire;
    private final MouseControllerSelect mouseSelect;
    private final MouseControllerMoveSelected mouseMoveSelected;
    private final MouseControllerCopySelected mouseCopySelected;
    private final MouseController mouseRun;

    private Circuit circuit;
    private MouseController activeMouseController;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;

    /**
     * Creates a new instance
     *
     * @param aCircuit     the circuit to show
     * @param library      the library used to edit the attributes of the elements
     * @param shapeFactory the shapeFactory used for copied elements
     */
    public CircuitComponent(Circuit aCircuit, ElementLibrary library, ShapeFactory shapeFactory) {
        this.library = library;
        this.shapeFactory = shapeFactory;
        highLighted = new HashSet<>();

        deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.delete();
            }
        }.setToolTip(Lang.get("menu_delete_tt"));

        KeyStroke delKey = KeyStroke.getKeyStroke("DELETE");
        getInputMap().put(delKey, DEL_ACTION);
        getActionMap().put(DEL_ACTION, deleteAction);

        setFocusable(true);

        addMouseWheelListener(e -> {
            Vector pos = getPosVector(e);
            double f = Math.pow(0.9, e.getWheelRotation());
            transform.translate(pos.x, pos.y);
            transform.scale(f, f);
            transform.translate(-pos.x, -pos.y);
            repaint();
        });

        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        mouseNormal = new MouseControllerNormal(normalCursor);
        mouseInsertElement = new MouseControllerInsertElement(normalCursor);
        mouseMoveElement = new MouseControllerMoveElement(normalCursor);
        mouseWire = new MouseControllerWire(normalCursor);
        mouseSelect = new MouseControllerSelect(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mouseMoveSelected = new MouseControllerMoveSelected(new Cursor(Cursor.MOVE_CURSOR));
        mouseCopySelected = new MouseControllerCopySelected(new Cursor(Cursor.MOVE_CURSOR));
        mouseRun = new MouseControllerRun(normalCursor);

        setCircuit(aCircuit);

        MouseDispatcher disp = new MouseDispatcher();
        addMouseMotionListener(disp);
        addMouseListener(disp);

    }

    /**
     * @return the delete action to put it to the toolbar
     */
    public ToolTipAction getDeleteAction() {
        return deleteAction;
    }

    /**
     * Sets the observer to call if the user is clicking on elements while running.
     *
     * @param callOnManualChange the listener
     */
    public void setManualChangeObserver(Observer callOnManualChange) {
        this.manualChangeObserver = callOnManualChange;
    }

    /**
     * Sets the edit mode and resets the circuit
     *
     * @param runMode true if running, false if editing
     */
    public void setModeAndReset(boolean runMode) {
        if (runMode)
            mouseRun.activate();
        else
            mouseNormal.activate();
        requestFocusInWindow();
        circuit.clearState();
    }

    /**
     * @return the high lighted elements
     */
    public Collection<Drawable> getHighLighted() {
        return highLighted;
    }

    /**
     * Adds a drawable to the highlighted list
     *
     * @param drawable the drawable to add
     * @param <T>      type of drawable
     */
    public <T extends Drawable> void addHighLighted(T drawable) {
        highLighted.add(drawable);
    }

    /**
     * Add a list of drawables to high light
     *
     * @param drawables the list of drawables
     */
    public void addHighLighted(Collection<? extends Drawable> drawables) {
        highLighted.addAll(drawables);
    }

    /**
     * Adds alle the wires representing the given value to the highlighted list
     *
     * @param values the value
     */
    public void addHighLightedWires(ObservableValue[] values) {
        HashSet<ObservableValue> ov = new HashSet<>();
        ov.addAll(Arrays.asList(values));
        for (Wire w : circuit.getWires())
            if (ov.contains(w.getValue()))
                addHighLighted(w);
    }

    /**
     * remove all highlighted elements
     */
    public void removeHighLighted() {
        highLighted.clear();
    }

    /**
     * Addes the given element to insert to the circuit
     *
     * @param element the element to insert
     */
    public void setPartToInsert(VisualElement element) {
        mouseInsertElement.activate(element);
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

        activeMouseController.drawTo(gr);
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

    /**
     * @return the circuit shown
     */
    public Circuit getCircuit() {
        return circuit;
    }

    /**
     * Sets a circuit to this component
     * @param circuit the circuit
     */
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

        setModeAndReset(false);
    }

    private void editAttributes(VisualElement vp, MouseEvent e) {
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
            }
        }
    }

    private class MouseDispatcher extends MouseAdapter implements MouseMotionListener {
        private Vector pos;

        @Override
        public void mouseClicked(MouseEvent e) {
            activeMouseController.clicked(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            pos = new Vector(e.getX(), e.getY());
            activeMouseController.pressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            activeMouseController.released(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            activeMouseController.moved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (!activeMouseController.dragged(e)) {
                Vector newPos = new Vector(e.getX(), e.getY());
                Vector delta = newPos.sub(pos);
                double s = transform.getScaleX();
                transform.translate(delta.x / s, delta.y / s);
                pos = newPos;
                repaint();
            }
        }

    }

    private class MouseController {
        private final Cursor mouseCursor;

        private MouseController(Cursor mouseCursor) {
            this.mouseCursor = mouseCursor;
        }

        private void activate() {
            activeMouseController = this;
            deleteAction.setActive(false);
            setCursor(mouseCursor);
            repaint();
        }

        void clicked(MouseEvent e) {
        }

        void pressed(MouseEvent e) {
        }

        void released(MouseEvent e) {
        }

        void moved(MouseEvent e) {
        }

        boolean dragged(MouseEvent e) {
            return false;
        }

        public void drawTo(Graphic gr) {
        }

        public void delete() {
        }
    }

    private final class MouseControllerNormal extends MouseController {
        private Vector pos;
        private int downButton;

        private MouseControllerNormal(Cursor cursor) {
            super(cursor);
        }

        @Override
        void clicked(MouseEvent e) {
            Vector pos = getPosVector(e);
            VisualElement vp = circuit.getElementAt(pos);
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (vp != null)
                    editAttributes(vp, e);
            } else if (e.getButton() == MouseEvent.BUTTON1) {
                if (vp != null) {
                    if (circuit.isPinPos(raster(pos), vp))
                        mouseWire.activate(pos);
                    else
                        mouseMoveElement.activate(vp, pos);
                } else {
                    mouseWire.activate(pos);
                }
            }
        }

        @Override
        void pressed(MouseEvent e) {
            downButton = e.getButton();
            pos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (downButton == MouseEvent.BUTTON1) {
                mouseSelect.activate(pos, getPosVector(e));
                return true;
            }
            return false;
        }
    }

    private final class MouseControllerInsertElement extends MouseController {
        private VisualElement element;
        private Vector delta;

        private MouseControllerInsertElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement element) {
            super.activate();
            this.element = element;
            delta = null;
            deleteAction.setActive(true);
        }

        @Override
        void moved(MouseEvent e) {
            if (delta == null) {
                GraphicMinMax minMax = element.getMinMax();
                delta = element.getPos().sub(minMax.getMax());
            }
            element.setPos(raster(getPosVector(e).add(delta)));
            repaint();
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        public void drawTo(Graphic gr) {
            if (delta != null)
                element.drawTo(gr, true);
        }

        @Override
        void clicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                circuit.add(element);
                repaint();
            }
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveElement extends MouseController {
        private VisualElement visualElement;
        private Vector delta;

        private MouseControllerMoveElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement visualElement, Vector pos) {
            super.activate();
            this.visualElement = visualElement;
            delta = visualElement.getPos().sub(pos);
            deleteAction.setActive(true);
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            visualElement.setPos(raster(visualElement.getPos()));
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            Vector pos = getPosVector(e);
            visualElement.setPos(raster(pos.add(delta)));
            repaint();
        }

        @Override
        public void drawTo(Graphic gr) {
            visualElement.drawTo(gr, true);
        }

        @Override
        public void delete() {
            circuit.delete(visualElement);
            mouseNormal.activate();
        }
    }

    private final class MouseControllerWire extends MouseController {
        private Wire wire;

        private MouseControllerWire(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector startPos) {
            super.activate();
            Vector pos = raster(startPos);
            wire = new Wire(pos, pos);
        }

        @Override
        void moved(MouseEvent e) {
            wire.setP2(raster(getPosVector(e)));
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3)
                mouseNormal.activate();
            else {
                circuit.add(wire);
                if (circuit.isPinPos(wire.p2))
                    mouseNormal.activate();
                else
                    wire = new Wire(wire.p2, wire.p2);
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            wire.drawTo(gr, false);
        }
    }

    private final class MouseControllerSelect extends MouseController {
        private Vector corner1;
        private Vector corner2;
        private int downButton;
        private boolean wasReleased;

        private MouseControllerSelect(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2) {
            super.activate();
            this.corner1 = corner1;
            this.corner2 = corner2;
            deleteAction.setActive(true);
            wasReleased = false;
        }

        @Override
        void clicked(MouseEvent e) {
            mouseNormal.activate();
        }

        @Override
        void pressed(MouseEvent e) {
            downButton = e.getButton();
        }

        @Override
        void released(MouseEvent e) {
            wasReleased = true;
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (wasReleased) {
                if (downButton == MouseEvent.BUTTON1)
                    mouseMoveSelected.activate(corner1, corner2, getPosVector(e));
                else if (downButton == MouseEvent.BUTTON3)
                    mouseCopySelected.activate(corner1, corner2, getPosVector(e));
            } else {
                corner2 = getPosVector(e);
                repaint();
            }
            return true;
        }

        @Override
        public void drawTo(Graphic gr) {
            Vector p1 = new Vector(corner1.x, corner2.y);
            Vector p2 = new Vector(corner2.x, corner1.y);
            gr.drawLine(corner1, p1, Style.DASH);
            gr.drawLine(p1, corner2, Style.DASH);
            gr.drawLine(corner2, p2, Style.DASH);
            gr.drawLine(p2, corner1, Style.DASH);
        }

        @Override
        public void delete() {
            circuit.delete(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveSelected extends MouseController {
        private ArrayList<Moveable> elements;
        private Vector lastPos;

        private MouseControllerMoveSelected(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2, Vector pos) {
            super.activate();
            lastPos = pos;
            elements = circuit.getElementsToMove(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Moveable m : elements)
                        m.move(delta);
                    circuit.modified();

                    repaint();
                    lastPos = lastPos.add(delta);
                }
            }
            return true;
        }

        @Override
        void released(MouseEvent e) {
            mouseNormal.activate();
        }
    }

    private final class MouseControllerCopySelected extends MouseController {
        private ArrayList<Moveable> elements;
        private Vector lastPos;
        private Vector movement;

        private MouseControllerCopySelected(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2, Vector pos) {
            super.activate();
            lastPos = pos;
            movement = new Vector(0, 0);
            deleteAction.setActive(true);
            elements = circuit.getElementsToCopy(Vector.min(corner1, corner2), Vector.max(corner1, corner2), shapeFactory);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Moveable m : elements)
                        m.move(delta);

                    movement = movement.add(delta);

                    repaint();
                    lastPos = lastPos.add(delta);
                }
            }
            return true;
        }

        @Override
        public void drawTo(Graphic gr) {
            if (elements != null)
                for (Moveable m : elements)
                    if (m instanceof Drawable)
                        ((Drawable) m).drawTo(gr, true);
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        void released(MouseEvent e) {
            if (elements != null && !movement.isZero()) {
                for (Moveable m : elements) {
                    if (m instanceof Wire)
                        circuit.add((Wire) m);
                    if (m instanceof VisualElement)
                        circuit.add((VisualElement) m);
                }
            }
            mouseNormal.activate();
        }
    }

    private final class MouseControllerRun extends MouseController {

        private MouseControllerRun(Cursor cursor) {
            super(cursor);
        }

        @Override
        void clicked(MouseEvent e) {
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

}
