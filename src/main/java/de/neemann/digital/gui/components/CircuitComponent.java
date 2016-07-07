package de.neemann.digital.gui.components;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.ImmutableList;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.LibrarySelector;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.SavedListener;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.IconCreator;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

/**
 * @author hneemann
 */
public class CircuitComponent extends JComponent {
    /**
     * The delete icon, also used from {@link de.neemann.digital.gui.components.terminal.TerminalDialog}
     */
    public static final Icon ICON_DELETE = IconCreator.create("delete.png");
    private static final String DEL_ACTION = "myDelAction";

    private final Main parent;
    private final ElementLibrary library;
    private final SavedListener parentsSavedListener;
    private final HashSet<Drawable> highLighted;
    private final ToolTipAction deleteAction;

    private final MouseController mouseNormal;
    private final MouseControllerInsertElement mouseInsertElement;
    private final MouseControllerMoveElement mouseMoveElement;
    private final MouseControllerWire mouseWire;
    private final MouseControllerSelect mouseSelect;
    private final MouseControllerMoveSelected mouseMoveSelected;
    private final MouseController mouseRun;
    private final MouseControllerInsertCopied mouseInsertList;
    private final Cursor moveCursor;
    private final AbstractAction copyAction;
    private final AbstractAction pasteAction;
    private final AbstractAction rotateAction;

    private Circuit circuit;
    private MouseController activeMouseController;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;
    private Vector lastMousePos;


    /**
     * Creates a new instance
     *
     * @param library      the library used to edit the attributes of the elements
     * @param shapeFactory the shapeFactory used for copied elements
     */
    public CircuitComponent(Main parent, ElementLibrary library, ShapeFactory shapeFactory, SavedListener parentsSavedListener) {
        this.parent = parent;
        this.library = library;
        this.parentsSavedListener = parentsSavedListener;
        highLighted = new HashSet<>();

        rotateAction = new AbstractAction(Lang.get("menu_rotate")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.rotate();
            }
        };
        rotateAction.setEnabled(false);


        copyAction = new AbstractAction(Lang.get("menu_copy")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController instanceof MouseControllerSelect) {
                    MouseControllerSelect mcs = ((MouseControllerSelect) activeMouseController);
                    ArrayList<Moveable> elements = circuit.getElementsToCopy(Vector.min(mcs.corner1, mcs.corner2), Vector.max(mcs.corner1, mcs.corner2), shapeFactory);
                    Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clpbrd.setContents(new CircuitTransferable(elements), null);
                    removeHighLighted();
                    mouseNormal.activate();
                }
            }
        };
        copyAction.setEnabled(false);

        pasteAction = new AbstractAction(Lang.get("menu_paste")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    Object data = clpbrd.getData(DataFlavor.stringFlavor);
                    if (data instanceof String) {
                        Vector posVector = getPosVector(lastMousePos.x, lastMousePos.y);
                        ArrayList<Moveable> elements = CircuitTransferable.createList(data, shapeFactory, posVector);
                        if (elements != null) {
                            mouseInsertList.activate(elements, posVector);
                        }
                    }
                } catch (UnsupportedFlavorException | IOException e1) {
                    e1.printStackTrace();
                }
            }
        };

        deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.delete();
            }
        }.setToolTip(Lang.get("menu_delete_tt"));

        getInputMap().put(KeyStroke.getKeyStroke("DELETE"), DEL_ACTION);
        getActionMap().put(DEL_ACTION, deleteAction);
        getInputMap().put(KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "myCopy");
        getActionMap().put("myCopy", copyAction);
        getInputMap().put(KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "myPaste");
        getActionMap().put("myPaste", pasteAction);
        getInputMap().put(KeyStroke.getKeyStroke("R"), "myRotate");
        getActionMap().put("myRotate", rotateAction);


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
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        mouseNormal = new MouseControllerNormal(normalCursor);
        mouseInsertElement = new MouseControllerInsertElement(normalCursor);
        mouseInsertList = new MouseControllerInsertCopied(normalCursor);
        mouseMoveElement = new MouseControllerMoveElement(normalCursor);
        mouseWire = new MouseControllerWire(normalCursor);
        mouseSelect = new MouseControllerSelect(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mouseMoveSelected = new MouseControllerMoveSelected(moveCursor);
        mouseRun = new MouseControllerRun(normalCursor);

        setCircuit(new Circuit());

        MouseDispatcher disp = new MouseDispatcher();
        addMouseMotionListener(disp);
        addMouseListener(disp);

        setToolTipText("");
    }

    /**
     * @return the main frame
     */
    public Main getMain() {
        return parent;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        Vector pos = getPosVector(event);
        VisualElement ve = circuit.getElementAt(pos);
        if (ve == null) return null;

        Pin p = circuit.getPinAt(raster(pos), ve);
        if (p != null)
            return checkToolTip(p.getDescription());

        ElementTypeDescription etd = library.getElementType(ve.getElementName());
        return checkToolTip(etd.getDescription(ve.getElementAttributes()));
    }

    private String checkToolTip(String tt) {
        if (tt != null && tt.length() == 0)
            return null;
        else
            return tt;
    }

    /**
     * @return the delete action to put it to the toolbar
     */
    public ToolTipAction getDeleteAction() {
        return deleteAction;
    }

    /**
     * @return the copy action
     */
    public AbstractAction getCopyAction() {
        return copyAction;
    }

    /**
     * @return the paste action
     */
    public AbstractAction getPasteAction() {
        return pasteAction;
    }

    /**
     * @return the rotate action
     */
    public AbstractAction getRotateAction() {
        return rotateAction;
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
        if (drawable != null)
            highLighted.add(drawable);
    }

    /**
     * Add a list of drawables to high light
     *
     * @param drawables the list of drawables
     */
    public void addHighLighted(Collection<? extends Drawable> drawables) {
        if (drawables != null)
            highLighted.addAll(drawables);
    }

    /**
     * Adds alle the wires representing the given value to the highlighted list
     *
     * @param values the value
     */
    public void addHighLightedWires(ImmutableList<ObservableValue> values) {
        if (values == null) return;

        HashSet<ObservableValue> ov = new HashSet<>();
        ov.addAll(values);
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
        return getPosVector(e.getX(), e.getY());
    }

    private Vector getPosVector(int x, int y) {
        try {
            Point2D.Double p = new Point2D.Double();
            transform.inverseTransform(new Point(x, y), p);
            return new Vector((int) Math.round(p.getX()), (int) Math.round(p.getY()));
        } catch (NoninvertibleTransformException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * rounds the given vector to the raster
     *
     * @param pos the vector
     * @return pos round to raster
     */
    public static Vector raster(Vector pos) {
        return new Vector((int) Math.round((double) pos.x / SIZE) * SIZE,
                (int) Math.round((double) pos.y / SIZE) * SIZE);
    }

    /**
     * @return the circuit shown
     */
    public Circuit getCircuit() {
        return circuit;
    }

    /**
     * Sets a circuit to this component
     *
     * @param circuit the circuit
     */
    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
        fitCircuit();
        setModeAndReset(false);
    }

    /**
     * maximizes the circuit shown
     */
    public void fitCircuit() {
        GraphicMinMax gr = new GraphicMinMax();
        circuit.drawTo(gr);
        if (gr.getMin() != null && getWidth() != 0 && getHeight() != 0) {

            Vector delta = gr.getMax().sub(gr.getMin());
            double sx = ((double) getWidth()) / (delta.x + Style.NORMAL.getThickness() * 2);
            double sy = ((double) getHeight()) / (delta.y + Style.NORMAL.getThickness() * 2);
            double s = Math.min(sx, sy);

            transform.setToScale(s, s);  // set Scaling

            Vector center = gr.getMin().add(gr.getMax()).div(2);
            transform.translate(-center.x, -center.y);  // move drawing center to (0,0)

            Vector dif = new Vector(getWidth(), getHeight()).div(2);
            transform.translate(dif.x / s, dif.y / s);  // move drawing center to frame center
        } else
            transform = new AffineTransform();
        repaint();
    }

    /**
     * scales the circuit
     *
     * @param f factor to scale
     */
    public void scaleCircuit(double f) {
        Vector dif = getPosVector(getWidth() / 2, getHeight() / 2);
        transform.translate(dif.x, dif.y);
        transform.scale(f, f);
        transform.translate(-dif.x, -dif.y);
        repaint();
    }

    private void editAttributes(VisualElement vp, MouseEvent e) {
        String name = vp.getElementName();
        ElementTypeDescription elementType = library.getElementType(name);
        ArrayList<Key> list = elementType.getAttributeList();
        if (list.size() > 0) {
            Point p = new Point(e.getX(), e.getY());
            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
            AttributeDialog attributeDialog = new AttributeDialog(this, p, list, vp.getElementAttributes());
            if (elementType instanceof LibrarySelector.ElementTypeDescriptionCustom) {
                attributeDialog.addButton(Lang.get("attr_openCircuitLabel"), new ToolTipAction(Lang.get("attr_openCircuit")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        attributeDialog.dispose();
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                new Main(CircuitComponent.this,
                                        ((LibrarySelector.ElementTypeDescriptionCustom) elementType).getFile(),
                                        new SavedListener() {
                                            @Override
                                            public void saved(File filename) {
                                                if (parentsSavedListener != null)
                                                    parentsSavedListener.saved(filename);
                                                library.removeElement(filename);
                                                circuit.clearState();
                                                repaint();
                                            }
                                        }).setVisible(true);
                            }
                        });
                    }
                }.setToolTip(Lang.get("attr_openCircuit_tt")));
            }
            if (attributeDialog.showDialog()) {
                circuit.modified();
                repaint();
            }
        }
    }

    private class MouseDispatcher extends MouseAdapter implements MouseMotionListener {
        private Vector pos;
        private boolean isMoved;

        @Override
        public void mousePressed(MouseEvent e) {
            pos = new Vector(e.getX(), e.getY());
            isMoved = false;
            requestFocusInWindow();
            activeMouseController.pressed(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            activeMouseController.released(e);
            if (!(wasMoved(e) || isMoved))
                activeMouseController.clicked(e);
        }

        private boolean wasMoved(MouseEvent e) {
            Vector d = new Vector(e.getX(), e.getY()).sub(pos);
            return Math.abs(d.x) > SIZE2 || Math.abs(d.y) > SIZE2;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            lastMousePos = new Vector(e.getX(), e.getY());
            activeMouseController.moved(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            lastMousePos = new Vector(e.getX(), e.getY());
            activeMouseController.moved(e);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (wasMoved(e) || isMoved) {
                isMoved = true;
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

    }

    private class MouseController {
        private final Cursor mouseCursor;

        private MouseController(Cursor mouseCursor) {
            this.mouseCursor = mouseCursor;
        }

        private void activate() {
            activeMouseController = this;
            deleteAction.setActive(false);
            copyAction.setEnabled(false);
            rotateAction.setEnabled(false);
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

        public void rotate() {
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
            rotateAction.setEnabled(true);
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

        @Override
        public void rotate() {
            element.rotate();
            repaint();
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
            rotateAction.setEnabled(true);
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
            circuit.modified();
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

        @Override
        public void rotate() {
            visualElement.rotate();
            circuit.modified();
            repaint();
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
        private static final int MIN_SIZE = 8;
        private Vector corner1;
        private Vector corner2;
        private boolean wasReleased;

        private MouseControllerSelect(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2) {
            super.activate();
            this.corner1 = corner1;
            this.corner2 = corner2;
            deleteAction.setActive(true);
            copyAction.setEnabled(true);
            rotateAction.setEnabled(true);
            wasReleased = false;
        }

        @Override
        void clicked(MouseEvent e) {
            mouseNormal.activate();
            removeHighLighted();
        }

        @Override
        void released(MouseEvent e) {
            wasReleased = true;
            Vector dif = corner1.sub(corner2);
            if (Math.abs(dif.x) > MIN_SIZE && Math.abs(dif.y) > MIN_SIZE)
                setCursor(moveCursor);
            else {
                removeHighLighted();
                mouseNormal.activate();
            }
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (wasReleased) {
                mouseMoveSelected.activate(corner1, corner2, getPosVector(e));
            } else {
                corner2 = getPosVector(e);
                if ((e.getModifiersEx() & CTRL_DOWN_MASK) != 0) {
                    Vector dif = corner2.sub(corner1);
                    int dx = dif.x;
                    int dy = dif.y;
                    int absDx = Math.abs(dx);
                    int absDy = Math.abs(dy);
                    if (absDx != absDy) {
                        if (absDx > absDy) {
                            if (dx > absDy) dx = absDy;
                            else dx = -absDy;
                        } else {
                            if (dy > absDx) dy = absDx;
                            else dy = -absDx;
                        }
                    }
                    corner2 = corner1.add(dx, dy);
                }

                ArrayList<Drawable> elements = circuit.getElementsToHighlight(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                removeHighLighted();
                if (elements != null)
                    addHighLighted(elements);

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

        public void rotate() {
            mouseMoveSelected.activate(corner1, corner2, lastMousePos);
            mouseMoveSelected.rotate();
        }
    }

    private void rotateElements(ArrayList<Moveable> elements, Vector pos) {
        Vector p1 = raster(pos);

        Transform transform = new TransformRotate(p1, 1) {
            @Override
            public Vector transform(Vector v) {
                return super.transform(v.sub(p1));
            }
        };

        for (Moveable m : elements) {

            if (m instanceof VisualElement) {
                VisualElement ve = (VisualElement) m;
                ve.rotate();
                ve.setPos(transform.transform(ve.getPos()));
            } else if (m instanceof Wire) {
                Wire w = (Wire) m;
                w.p1 = transform.transform(w.p1);
                w.p2 = transform.transform(w.p2);
            } else {
                Vector p = m.getPos();
                Vector t = transform.transform(p);
                m.move(t.sub(p));
            }

        }

        circuit.modified();
        repaint();
    }


    private final class MouseControllerMoveSelected extends MouseController {
        private ArrayList<Moveable> elements;
        private Vector lastPos;
        private Vector center;
        private boolean wasMoved;

        private MouseControllerMoveSelected(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2, Vector pos) {
            super.activate();
            rotateAction.setEnabled(true);
            lastPos = pos;
            center = corner1.add(corner2).div(2);
            wasMoved = false;
            elements = circuit.getElementsToMove(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
        }

        @Override
        void moved(MouseEvent e) {
            lastPos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Moveable m : elements)
                        m.move(delta);
                    wasMoved = true;

                    repaint();
                    lastPos = lastPos.add(delta);
                    center = center.add(delta);
                }
            }
            return true;
        }

        @Override
        void released(MouseEvent e) {
            if (wasMoved)
                circuit.elementsMoved();
            removeHighLighted();
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            rotateElements(elements, center);
        }
    }

    private final class MouseControllerInsertCopied extends MouseController {
        private ArrayList<Moveable> elements;
        private Vector lastPos;

        private MouseControllerInsertCopied(Cursor cursor) {
            super(cursor);
        }

        private void activate(ArrayList<Moveable> elements, Vector pos) {
            super.activate();
            this.elements = elements;
            lastPos = pos;
            deleteAction.setActive(true);
            rotateAction.setEnabled(true);
        }

        @Override
        void moved(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Moveable m : elements)
                        m.move(delta);

                    repaint();
                    lastPos = lastPos.add(delta);
                }
            }
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
        void clicked(MouseEvent e) {
            if (elements != null) {
                for (Moveable m : elements) {
                    if (m instanceof Wire)
                        circuit.add((Wire) m);
                    if (m instanceof VisualElement)
                        circuit.add((VisualElement) m);
                }
            }
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            rotateElements(elements, lastPos);
        }
    }


    private interface Actor {
        boolean interact(CircuitComponent cc, Point p);
    }

    private final class MouseControllerRun extends MouseController {

        private MouseControllerRun(Cursor cursor) {
            super(cursor);
        }

        @Override
        void pressed(MouseEvent e) {
            VisualElement ve = circuit.getElementAt(getPosVector(e));
            if (ve != null)
                interact(e, ve::elementPressed);
        }

        @Override
        void released(MouseEvent e) {
            VisualElement ve = circuit.getElementAt(getPosVector(e));
            if (ve != null)
                interact(e, ve::elementReleased);
        }

        @Override
        void clicked(MouseEvent e) {
            VisualElement ve = circuit.getElementAt(getPosVector(e));
            if (ve != null)
                interact(e, ve::elementClicked);
        }


        private void interact(MouseEvent e, Actor actor) {
            Point p = new Point(e.getX(), e.getY());
            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
            boolean modelHasChanged = actor.interact(CircuitComponent.this, p);
            if (modelHasChanged) {
                if (manualChangeObserver != null)
                    manualChangeObserver.hasChanged();
            } else
                repaint();
        }
    }

}
