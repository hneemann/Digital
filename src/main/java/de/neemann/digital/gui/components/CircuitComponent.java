/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.switching.Switch;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.library.LibraryListener;
import de.neemann.digital.draw.library.LibraryNode;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.InputShape;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.modification.*;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Component which shows the circuit.
 * ToDo: refactoring of repaint logic. Its to complex now.
 * ToDo: class is to large, move the MouseController classes to their own package
 */
public class CircuitComponent extends JComponent implements Circuit.ChangedListener, LibraryListener {
    /**
     * The delete icon, also used from {@link de.neemann.digital.gui.components.terminal.TerminalDialog}
     */
    public static final Icon ICON_DELETE = IconCreator.create("delete.png");
    private static final Icon ICON_UNDO = IconCreator.create("edit-undo.png");
    private static final Icon ICON_REDO = IconCreator.create("edit-redo.png");
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();

    static {
        ATTR_LIST.add(Keys.WIDTH);
        ATTR_LIST.add(Keys.SHAPE_TYPE);
        if (Main.isExperimentalMode())
            ATTR_LIST.add(Keys.CUSTOM_SHAPE);
        ATTR_LIST.add(Keys.HEIGHT);
        ATTR_LIST.add(Keys.PINCOUNT);
        ATTR_LIST.add(Keys.BACKGROUND_COLOR);
        ATTR_LIST.add(Keys.DESCRIPTION);
        ATTR_LIST.add(Keys.LOCKED_MODE);
        ATTR_LIST.add(Keys.ROMMANAGER);
        ATTR_LIST.add(Keys.SHOW_DATA_TABLE);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH);
        ATTR_LIST.add(Keys.SHOW_DATA_GRAPH_MICRO);
        ATTR_LIST.add(Keys.PRELOAD_PROGRAM);
        ATTR_LIST.add(Keys.PROGRAM_TO_PRELOAD);
    }

    private static final String DEL_ACTION = "myDelAction";
    private static final int MOUSE_BORDER_SMALL = 10;
    private static final int MOUSE_BORDER_LARGE = 50;

    private static final int DRAG_DISTANCE = (int) (SIZE2 * Screen.getInstance().getScaling());

    private static final Color GRID_COLOR = new Color(210, 210, 210);

    private final Main parent;
    private final ElementLibrary library;
    private final HashSet<Drawable> highLighted;
    private final ToolTipAction deleteAction;

    private final MouseController mouseNormal;
    private final MouseControllerInsertElement mouseInsertElement;
    private final MouseControllerMoveElement mouseMoveElement;
    private final MouseControllerMoveWire mouseMoveWire;
    private final MouseControllerWireDiag mouseWireDiag;
    private final MouseControllerWireRect mouseWireRect;
    private final MouseControllerWireSplit mouseWireSplit;
    private final MouseControllerSelect mouseSelect;
    private final MouseControllerMoveSelected mouseMoveSelected;
    private final MouseController mouseRun;
    private final MouseControllerInsertCopied mouseInsertList;
    private final Cursor moveCursor;
    private final ToolTipAction copyAction;
    private final ToolTipAction cutAction;
    private final ToolTipAction pasteAction;
    private final ToolTipAction rotateAction;
    private final ToolTipAction undoAction;
    private final ToolTipAction redoAction;

    private Circuit circuit;
    private MouseController activeMouseController;
    private AffineTransform transform = new AffineTransform();
    private Observer manualChangeObserver;
    private Vector lastMousePos;
    private SyncAccess modelSync;
    private boolean isManualScale;
    private boolean graphicsHasChanged = true;
    private boolean focusWasLost = false;
    private boolean lockMessageShown = false;
    private boolean antiAlias = true;

    private ArrayList<Modification> modifications;
    private Circuit initialCircuit;
    private int undoPosition;
    private int savedUndoPosition;
    private Style highLightStyle = Style.HIGHLIGHT;
    private Mouse mouse = Mouse.getMouse();


    /**
     * Creates a new instance
     *
     * @param parent       the parent window
     * @param library      the library used to edit the attributes of the elements
     * @param shapeFactory the shapeFactory used for copied elements
     */
    public CircuitComponent(Main parent, ElementLibrary library, ShapeFactory shapeFactory) {
        this.parent = parent;
        this.library = library;
        highLighted = new HashSet<>();

        rotateAction = new ToolTipAction(Lang.get("menu_rotate")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.rotate();
            }
        }.setEnabledChain(false).setAccelerator("R").enableAcceleratorIn(this);


        cutAction = createCutAction(shapeFactory);
        copyAction = createCopyAction(shapeFactory);
        pasteAction = createPasteAction(shapeFactory);

        deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.delete();
            }
        }.setToolTip(Lang.get("menu_delete_tt"));

        undoAction = new ToolTipAction(Lang.get("menu_undo"), ICON_UNDO) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                undo();
            }
        }.setToolTipProvider(this::getUndoToolTip).setToolTip(Lang.get("menu_undo_tt")).setAcceleratorCTRLplus('Z').enableAcceleratorIn(this);

        redoAction = new ToolTipAction(Lang.get("menu_redo"), ICON_REDO) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                redo();
            }
        }.setToolTipProvider(this::getRedoToolTip).setToolTip(Lang.get("menu_redo_tt")).setAcceleratorCTRLplus('Y').enableAcceleratorIn(this);

        new ToolTipAction("Escape") {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeMouseController.escapePressed();
            }
        }.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0)).enableAcceleratorIn(this);

        new ToolTipAction("flipWire") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController instanceof MouseControllerWireRect)
                    ((MouseControllerWireRect) activeMouseController).flipWire();
            }
        }.setAccelerator("F").enableAcceleratorIn(this);

        new ToolTipAction("splitWire") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController == mouseNormal) {
                    Vector pos = getPosVector(lastMousePos.x, lastMousePos.y);
                    Wire w = circuit.getWireAt(pos, SIZE2);
                    if (w != null)
                        mouseWireSplit.activate(w, pos);
                }
            }
        }.setAccelerator("S").enableAcceleratorIn(this);


        createAdditionalShortcuts(shapeFactory);

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DEL_ACTION);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DEL_ACTION);
        getActionMap().put(DEL_ACTION, deleteAction);

        setFocusable(true);

        addMouseWheelListener(e -> {
            Vector pos = getPosVector(e);
            double f = Math.pow(0.9, e.getWheelRotation());
            transform.translate(pos.x, pos.y);
            transform.scale(f, f);
            transform.translate(-pos.x, -pos.y);
            isManualScale = true;
            repaintNeeded();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (!isManualScale)
                    fitCircuit();
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                focusWasLost = true;
            }
        });

        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        moveCursor = new Cursor(Cursor.MOVE_CURSOR);
        mouseNormal = new MouseControllerNormal(normalCursor);
        mouseInsertElement = new MouseControllerInsertElement(normalCursor);
        mouseInsertList = new MouseControllerInsertCopied(normalCursor);
        mouseMoveElement = new MouseControllerMoveElement(normalCursor);
        mouseMoveWire = new MouseControllerMoveWire(normalCursor);
        mouseWireRect = new MouseControllerWireRect(normalCursor);
        mouseWireDiag = new MouseControllerWireDiag(normalCursor);
        mouseWireSplit = new MouseControllerWireSplit(normalCursor);
        mouseSelect = new MouseControllerSelect(new Cursor(Cursor.CROSSHAIR_CURSOR));
        mouseMoveSelected = new MouseControllerMoveSelected(moveCursor);
        mouseRun = new MouseControllerRun(normalCursor);

        setCircuit(new Circuit());
        circuit.addListener(this);

        MouseDispatcher dispatcher = new MouseDispatcher();
        addMouseMotionListener(dispatcher);
        addMouseListener(dispatcher);

        setToolTipText("");
    }

    private void createAdditionalShortcuts(ShapeFactory shapeFactory) {
        new ToolTipAction("diagWire") {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (activeMouseController instanceof MouseControllerWireDiag)
                    ((MouseControllerWireDiag) activeMouseController).rectangularWire();
                else if (activeMouseController instanceof MouseControllerWireRect)
                    ((MouseControllerWireRect) activeMouseController).diagonalWire();
            }
        }.setAccelerator("D").enableAcceleratorIn(this);

        new ToolTipAction("selectAll") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (activeMouseController == mouseNormal) {
                    GraphicMinMax gr = new GraphicMinMax();
                    circuit.drawTo(gr);
                    if (gr.getMin() != null && gr.getMax() != null) {
                        mouseSelect.activate(gr.getMin(), gr.getMax());
                        mouseSelect.release();
                    }
                }
            }
        }.setAcceleratorCTRLplus('A').enableAcceleratorIn(this);

        new ToolTipAction("duplicate") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ArrayList<Movable> elements = getSelectedElements(shapeFactory);
                if (elements != null) {
                    activeMouseController.escapePressed();
                    ArrayList<Movable> copiedElements = new ArrayList<>();
                    for (Movable m : elements) {
                        if (m instanceof Wire)
                            copiedElements.add(new Wire((Wire) m));
                        else if (m instanceof VisualElement)
                            copiedElements.add(new VisualElement((VisualElement) m));
                    }
                    setPartsToInsert(copiedElements, null);
                }
            }
        }.setAcceleratorCTRLplus('D').enableAcceleratorIn(this);


        ToolTipAction plus = new PlusMinusAction(1).setAccelerator("PLUS").enableAcceleratorIn(this);
        // enable [+] which is SHIFT+[=] on english keyboard layout
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, false), plus);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0, false), plus);

        ToolTipAction minus = new PlusMinusAction(-1).setAccelerator("MINUS").enableAcceleratorIn(this);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0, false), minus);

        new ToolTipAction(Lang.get("menu_programDiode")) {
            @Override
            public void actionPerformed(ActionEvent e) { // is allowed also if locked!
                VisualElement ve = getActualVisualElement();
                if (ve != null) {
                    if (CircuitComponent.this.library.isProgrammable(ve.getElementName())) {
                        boolean blown = ve.getElementAttributes().get(Keys.BLOWN);
                        modify(new ModifyAttribute<>(ve, Keys.BLOWN, !blown));
                    } else if (ve.equalsDescription(Switch.DESCRIPTION)) {
                        boolean closed = ve.getElementAttributes().get(Keys.CLOSED);
                        modify(new ModifyAttribute<>(ve, Keys.CLOSED, !closed));
                    }
                }
            }
        }.setAccelerator("P").enableAcceleratorIn(this);
    }

    private ToolTipAction createPasteAction(ShapeFactory shapeFactory) {
        return new ToolTipAction(Lang.get("menu_paste")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isLocked()) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    try {
                        Object data = clipboard.getData(DataFlavor.stringFlavor);
                        if (data instanceof String) {
                            Vector posVector = getPosVector(lastMousePos.x, lastMousePos.y);
                            ArrayList<Movable> elements = CircuitTransferable.createList(data, shapeFactory, posVector);
                            if (elements != null)
                                setPartsToInsert(elements, posVector);
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        SwingUtilities.invokeLater(new ErrorMessage(Lang.get("msg_clipboardContainsNoImportableData")).setComponent(CircuitComponent.this));
                    }
                }
            }
        }.setAcceleratorCTRLplus('V').enableAcceleratorIn(this);
    }

    private ToolTipAction createCutAction(ShapeFactory shapeFactory) {
        return new ToolTipAction(Lang.get("menu_cut")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isLocked()) {
                    if (activeMouseController instanceof MouseControllerSelect) {
                        MouseControllerSelect mcs = ((MouseControllerSelect) activeMouseController);
                        Vector min = Vector.min(mcs.corner1, mcs.corner2);
                        Vector max = Vector.max(mcs.corner1, mcs.corner2);
                        ArrayList<Movable> elements = circuit.getElementsToCopy(min, max, shapeFactory);
                        if (elements != null) {
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(new CircuitTransferable(elements), null);
                            modify(new ModifyDeleteRect(min, max));
                            activeMouseController.escapePressed();
                        }
                    }
                }
            }
        }.setEnabledChain(false).setAcceleratorCTRLplus('X').enableAcceleratorIn(this);
    }

    private ToolTipAction createCopyAction(ShapeFactory shapeFactory) {
        return new ToolTipAction(Lang.get("menu_copy")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<Movable> elements = getSelectedElements(shapeFactory);
                if (elements != null) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new CircuitTransferable(elements), null);
                    activeMouseController.escapePressed();
                }
            }
        }.setEnabledChain(false).setAcceleratorCTRLplus('C').enableAcceleratorIn(this);
    }

    private ArrayList<Movable> getSelectedElements(ShapeFactory shapeFactory) {
        ArrayList<Movable> elements = null;
        if (activeMouseController instanceof MouseControllerSelect) {
            MouseControllerSelect mcs = ((MouseControllerSelect) activeMouseController);
            elements = circuit.getElementsToCopy(Vector.min(mcs.corner1, mcs.corner2), Vector.max(mcs.corner1, mcs.corner2), shapeFactory);
        } else if (activeMouseController instanceof MouseControllerMoveElement) {
            MouseControllerMoveElement mcme = ((MouseControllerMoveElement) activeMouseController);
            elements = new ArrayList<>();
            elements.add(mcme.visualElement);
        }
        return elements;
    }


    /**
     * Opens the attribute editor
     */
    public void editCircuitAttributes() {
        editCircuitAttributes(ATTR_LIST);
    }

    /**
     * Opens the attribute editor
     *
     * @param attrList the list of keys to edit
     */
    public void editCircuitAttributes(java.util.List<Key> attrList) {
        ElementAttributes modifiedAttributes = new AttributeDialog(parent, attrList, circuit.getAttributes()).showDialog();
        if (modifiedAttributes != null)
            modify(new ModifyCircuitAttributes(modifiedAttributes));
    }

    /**
     * Apply a modification
     *
     * @param modification the modification
     */
    public void modify(Modification modification) {
        if (modification != null) {
            modification.modify(circuit, library);
            addModificationAlreadyMade(modification);
        }
    }

    /**
     * Add a modification already made
     *
     * @param modification the modification
     */
    private void addModificationAlreadyMade(Modification modification) {
        if (modification != null) {
            while (modifications.size() > undoPosition)
                modifications.remove(modifications.size() - 1);
            redoAction.setEnabled(false);
            modifications.add(modification);
            undoPosition = modifications.size();
            undoAction.setEnabled(true);
            circuit.modified();
            repaintNeeded();
        }
    }

    /**
     * invalidates the image buffer and calls repaint();
     */
    public void repaintNeeded() {
        graphicsHasChanged = true;
        repaint();
    }

    /**
     * undo last action
     */
    private void undo() {
        if (!isLocked() && undoPosition > 0) {
            Circuit c = new Circuit(initialCircuit);
            c.getListenersFrom(circuit);
            circuit = c;
            undoPosition--;
            for (int i = 0; i < undoPosition; i++)
                modifications.get(i).modify(circuit, library);
            redoAction.setEnabled(true);
            if (undoPosition == 0)
                undoAction.setEnabled(false);
            circuit.setModified(undoPosition != savedUndoPosition);
            circuit.fireChangedEvent();
            repaintNeeded();
        }
    }

    private String getUndoToolTip() {
        if (undoPosition > 0)
            return Lang.get("mod_undo_N", modifications.get(undoPosition - 1).toString());
        else
            return Lang.get("menu_undo_tt");
    }

    /**
     * redo last undo
     */
    private void redo() {
        if (!isLocked() && undoPosition < modifications.size()) {
            modifications.get(undoPosition).modify(circuit, library);
            undoPosition++;
            if (undoPosition == modifications.size())
                redoAction.setEnabled(false);
            undoAction.setEnabled(true);
            circuit.setModified(undoPosition != savedUndoPosition);
            circuit.fireChangedEvent();
            repaintNeeded();
        }
    }

    private String getRedoToolTip() {
        if (undoPosition < modifications.size())
            return Lang.get("mod_redo_N", modifications.get(undoPosition).toString());
        else
            return Lang.get("menu_redo_tt");
    }

    /**
     * save the circuit
     *
     * @param filename the filename
     * @throws IOException IOException
     */
    public void save(File filename) throws IOException {
        circuit.save(filename);
        savedUndoPosition = undoPosition;
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
        if (ve != null) {
            Pin p = circuit.getPinAt(raster(pos), ve);
            if (p != null)
                return createPinToolTip(p);

            if (Settings.getInstance().get(Keys.SETTINGS_NOTOOLTIPS))
                return null;

            try {
                ElementTypeDescription etd = library.getElementType(ve.getElementName());
                String tt = etd.getDescription(ve.getElementAttributes());
                final String pin = ve.getElementAttributes().get(Keys.PINNUMBER);
                if (pin.length() > 0)
                    tt += " (" + Lang.get("msg_pin_N", pin) + ")";
                return checkToolTip(tt);
            } catch (ElementNotFoundException e) {
                return null;
            }
        }

        Wire w = circuit.getWireAt(pos, SIZE2);
        if (w != null) {
            ObservableValue v = w.getValue();
            if (v != null)
                return v.getValueString();
        }

        return null;
    }

    private String createPinToolTip(Pin p) {
        String text = p.getName();
        final String des = p.getDescription();
        if (des != null && des.length() > 0) {
            text += ": " + des;
        }
        String pinNumber = p.getPinNumber();
        if (pinNumber != null && pinNumber.length() > 0)
            text += " (" + Lang.get("msg_pin_N", pinNumber) + ")";
        return checkToolTip(text);
    }

    private String checkToolTip(String tt) {
        if (tt != null && tt.length() == 0)
            return null;
        else
            return new LineBreaker().toHTML().breakLines(tt);
    }

    /**
     * @return the delete action to put it to the toolbar
     */
    public ToolTipAction getDeleteAction() {
        return deleteAction;
    }

    /**
     * @return the cut action
     */
    public ToolTipAction getCutAction() {
        return cutAction;
    }

    /**
     * @return the copy action
     */
    public ToolTipAction getCopyAction() {
        return copyAction;
    }

    /**
     * @return the paste action
     */
    public ToolTipAction getPasteAction() {
        return pasteAction;
    }

    /**
     * @return the rotate action
     */
    public ToolTipAction getRotateAction() {
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
     * @param runMode   true if running, false if editing
     * @param modelSync used to access the running model
     */
    public void setModeAndReset(boolean runMode, SyncAccess modelSync) {
        this.modelSync = modelSync;
        if (runMode)
            mouseRun.activate();
        else {
            mouseNormal.activate();
            circuit.clearState();
        }
        requestFocusInWindow();
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
     * Adds all the wires representing the given value to the highlighted list
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
        highLightStyle = Style.HIGHLIGHT;
    }

    /**
     * Sets the style used to highlight components
     *
     * @param highLightStyle the style to highlight components
     */
    public void setHighLightStyle(Style highLightStyle) {
        this.highLightStyle = highLightStyle;
    }

    /**
     * @return the actual highlighted style
     */
    public Style getHighLightStyle() {
        return highLightStyle;
    }

    /**
     * Adds the given element to insert to the circuit
     *
     * @param element the element to insert
     */
    public void setPartToInsert(VisualElement element) {
        parent.ensureModelIsStopped();
        mouseInsertElement.activate(element);
        Point point = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(point, this);
        if (point.x < MOUSE_BORDER_LARGE || point.x > getWidth() - MOUSE_BORDER_SMALL
                || point.y < MOUSE_BORDER_LARGE || point.y > getHeight() - MOUSE_BORDER_SMALL) {

            if (point.x < MOUSE_BORDER_LARGE)
                point.x = MOUSE_BORDER_LARGE;
            else if (point.x > getWidth() - MOUSE_BORDER_SMALL)
                point.x = getWidth() - MOUSE_BORDER_SMALL;

            if (point.y < MOUSE_BORDER_LARGE)
                point.y = MOUSE_BORDER_LARGE;
            else if (point.y > getHeight() - MOUSE_BORDER_SMALL)
                point.y = getHeight() - MOUSE_BORDER_SMALL;

        }
        mouseInsertElement.updateMousePos(getPosVector(point.x, point.y));
        repaintNeeded();
        requestFocus();
    }

    /**
     * Adds the given list of elements to insert to the circuit
     *
     * @param elements the list of elements to insert
     * @param pos      inserting position, maybe null
     */
    public void setPartsToInsert(ArrayList<Movable> elements, Vector pos) {
        removeHighLighted();
        parent.ensureModelIsStopped();
        if (pos == null) {
            if (lastMousePos != null)
                pos = getPosVector(lastMousePos.x, lastMousePos.y);
            else
                pos = getPosVector(0, 0);
        }
        mouseInsertList.activate(elements, pos);
        repaintNeeded();
    }


    private BufferedImage buffer;
    private int highlightedPaintedSize;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean needsNewBuffer = buffer == null
                || getWidth() != buffer.getWidth()
                || getHeight() != buffer.getHeight();

        if (needsNewBuffer && !isManualScale)
            fitCircuit();

        final double scaleX = transform.getScaleX();
        if (graphicsHasChanged
                || needsNewBuffer
                || highLighted.size() != highlightedPaintedSize) {

            if (needsNewBuffer)
                buffer = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());

            Graphics2D gr2 = buffer.createGraphics();
            enableAntiAlias(gr2);
            gr2.setColor(Color.WHITE);
            gr2.fillRect(0, 0, getWidth(), getHeight());

            if (scaleX > 0.3 && Settings.getInstance().get(Keys.SETTINGS_GRID))
                drawGrid(gr2);

            gr2.transform(transform);

            GraphicSwing gr = new GraphicSwing(gr2, (int) (2 / scaleX));

            long time = System.currentTimeMillis();
            circuit.drawTo(gr, highLighted, highLightStyle, modelSync);
            time = System.currentTimeMillis() - time;

            if (time > 500) antiAlias = false;
            if (time < 50) antiAlias = true;

//            System.out.println("repaint: " + Long.toString(time) + "ms");

            highlightedPaintedSize = highLighted.size();
            graphicsHasChanged = false;
        }

        g.drawImage(buffer, 0, 0, null);

        Graphics2D gr2 = (Graphics2D) g;
        AffineTransform oldTrans = gr2.getTransform();
        gr2.transform(transform);
        enableAntiAlias(gr2);
        GraphicSwing gr = new GraphicSwing(gr2, (int) (2 / scaleX));
        activeMouseController.drawTo(gr);
        gr2.setTransform(oldTrans);
    }

    private void drawGrid(Graphics2D gr2) {
        Vector g1 = raster(getPosVector(0, 0));
        Point2D p1 = new Point2D.Double();
        transform.transform(new Point(g1.x, g1.y), p1);

        Vector g2 = raster(getPosVector(getWidth(), getHeight()));
        Point2D p2 = new Point2D.Double();
        transform.transform(new Point(g2.x, g2.y), p2);

        int cx = (g2.x - g1.x) / SIZE;
        int cy = (g2.y - g1.y) / SIZE;

        if (cx == 0 || cy == 0) return;

        float screenScaling = Screen.getInstance().getScaling();
        double delta = transform.getScaleX() * 2 * screenScaling;
        double min = 2 * screenScaling;
        if (delta < min) delta = min;
        double max = 8 * screenScaling;
        if (delta > max) delta = max;
        double sub = delta / 2.0;

        gr2.setColor(GRID_COLOR);
        for (int x = 0; x <= cx; x++) {
            double xx = p1.getX() + (p2.getX() - p1.getX()) * x / cx - sub;
            for (int y = 0; y <= cy; y++) {
                double yy = p1.getY() + (p2.getY() - p1.getY()) * y / cy - sub;
                gr2.fill(new Rectangle2D.Double(xx, yy, delta, delta));
            }
        }
    }

    /**
     * Transforms a circuit coordinate to a screen coordinate relative to this component.
     *
     * @param pos the circuit position
     * @return the screen coordinate relative to this component
     */
    public Point transform(Vector pos) {
        Point p = new Point();
        transform.transform(new Point(pos.x, pos.y), p);
        return p;
    }

    private void enableAntiAlias(Graphics2D gr2) {
        if (antiAlias) {
            gr2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gr2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            gr2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        }
    }

    @Override
    public void circuitHasChanged() {
        graphicsHasChanged = true;
    }

    /**
     * forces a immediately repaint
     * Is called from {@link de.neemann.digital.gui.GuiModelObserver} if the models data has changed.
     * Therefore the double buffer is invalidated.
     */
    public void paintImmediately() {
        graphicsHasChanged = true;
        paintImmediately(0, 0, getWidth(), getHeight());
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
    private static Vector raster(Vector pos) {
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
        if (this.circuit != null) {
            circuit.getListenersFrom(this.circuit);
        }

        this.circuit = circuit;
        modifications = new ArrayList<>();
        initialCircuit = new Circuit(circuit);
        undoPosition = 0;
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);

        fitCircuit();
        setModeAndReset(false, SyncAccess.NOSYNC);
    }

    /**
     * maximizes the circuit shown
     */
    public void fitCircuit() {
        GraphicMinMax gr = new GraphicMinMax();
        circuit.drawTo(gr);

        AffineTransform newTrans = new AffineTransform();
        if (gr.getMin() != null && getWidth() != 0 && getHeight() != 0) {
            Vector delta = gr.getMax().sub(gr.getMin());
            double sx = ((double) getWidth()) / (delta.x + Style.NORMAL.getThickness() * 2);
            double sy = ((double) getHeight()) / (delta.y + Style.NORMAL.getThickness() * 2);
            double s = Math.min(sx, sy);


            newTrans.setToScale(s, s);  // set Scaling

            Vector center = gr.getMin().add(gr.getMax()).div(2);
            newTrans.translate(-center.x, -center.y);  // move drawing center to (0,0)

            Vector dif = new Vector(getWidth(), getHeight()).div(2);
            newTrans.translate(dif.x / s, dif.y / s);  // move drawing center to frame center
            isManualScale = false;
        } else {
            isManualScale = true;
        }
        if (!newTrans.equals(transform)) {
            transform = newTrans;
            repaintNeeded();
        }
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
        isManualScale = true;
        repaintNeeded();
    }

    /**
     * Translates the circuit.
     *
     * @param dx x movement
     * @param dy y movement
     */
    public void translateCircuit(int dx, int dy) {
        transform.translate(dx, dy);
        isManualScale = true;
        repaintNeeded();
    }

    private void editAttributes(VisualElement element, MouseEvent e) {
        String name = element.getElementName();
        try {
            ElementTypeDescription elementType = library.getElementType(name);
            ArrayList<Key> list = elementType.getAttributeList();
            if (list.size() > 0) {
                Point p = new Point(e.getX(), e.getY());
                SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                AttributeDialog attributeDialog = new AttributeDialog(parent, p, list, element.getElementAttributes()).setVisualElement(element);
                if (elementType instanceof ElementLibrary.ElementTypeDescriptionCustom) {
                    attributeDialog.addButton(Lang.get("attr_openCircuitLabel"), new ToolTipAction(Lang.get("attr_openCircuit")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            attributeDialog.dispose();
                            new Main.MainBuilder()
                                    .setParent(parent)
                                    .setFileToOpen(((ElementLibrary.ElementTypeDescriptionCustom) elementType).getFile())
                                    .setLibrary(library)
                                    .denyMostFileActions()
                                    .keepPrefMainFile()
                                    .openLater();
                        }
                    }.setToolTip(Lang.get("attr_openCircuit_tt")));
                }
                attributeDialog.addButton(new ToolTipAction(Lang.get("attr_help")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        try {
                            new ElementHelpDialog(attributeDialog, elementType, element.getElementAttributes()).setVisible(true);
                        } catch (PinException | NodeException e1) {
                            new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e1).show(CircuitComponent.this);
                        }
                    }
                }.setToolTip(Lang.get("attr_help_tt")));

                ElementAttributes modified = attributeDialog.showDialog();
                if (modified != null)
                    modify(new ModifyAttributes(element, modified));
            }
        } catch (ElementNotFoundException ex) {
            // do nothing if element not found!
        }
    }

    @Override
    public void libraryChanged(LibraryNode node) {
        circuit.clearState();
        graphicsHasChanged = true;
        repaint();
    }

    /**
     * @return returns true if this circuit is locked
     */
    public boolean isLocked() {
        final boolean locked = circuit.getAttributes().get(Keys.LOCKED_MODE);
        if (locked && !lockMessageShown) {
            String message = Lang.get("msg_isLocked",
                    Lang.get("menu_edit"),
                    Lang.get("menu_editAttributes"),
                    Lang.get("key_lockedMode"));
            SwingUtilities.invokeLater(new ErrorMessage(message).setComponent(this));
            lockMessageShown = true;
        }
        return locked;
    }

    /**
     * @return undo action
     */
    public ToolTipAction getUndoAction() {
        return undoAction;
    }

    /**
     * @return redo action
     */
    public ToolTipAction getRedoAction() {
        return redoAction;
    }


    /**
     * Makes actual input values to the default value
     */
    public void actualToDefault() {
        if (!isLocked()) {
            Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_actualToDefault"));
            for (VisualElement ve : circuit.getElements())
                if (ve.equalsDescription(In.DESCRIPTION)) {
                    ObservableValue ov = ((InputShape) ve.getShape()).getObservableValue();
                    if (ov != null) {
                        InValue newValue = new InValue(ov);
                        InValue oldValue = ve.getElementAttributes().get(Keys.INPUT_DEFAULT);
                        if (!newValue.equals(oldValue))
                            builder.add(new ModifyAttribute<>(ve, Keys.INPUT_DEFAULT, newValue));
                    }
                }
            modify(builder.build());
        }
    }

    /**
     * All fuses (diodes) are restored to "not programed" so that they are working again.
     */
    public void restoreAllFuses() {
        Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_restoreAllFuses"));
        for (VisualElement ve : circuit.getElements())
            if (library.isProgrammable(ve.getElementName())) {
                if (ve.getElementAttributes().get(Keys.BLOWN))
                    builder.add(new ModifyAttribute<>(ve, Keys.BLOWN, false));
            }
        modify(builder.build());
    }

    /**
     * Label all inputs and outputs
     */
    public void labelPins() {
        LabelGenerator inGenerator = new LabelGenerator('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H');
        LabelGenerator outGenerator = new LabelGenerator('Y', 'X', 'Z', 'U', 'V');

        Modifications.Builder builder = new Modifications.Builder(Lang.get("menu_labelPins"));
        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION) && ve.getElementAttributes().getLabel().length() == 0) {
                builder.add(new ModifyAttribute<>(ve, Keys.LABEL, inGenerator.createLabel()));
            } else if (ve.equalsDescription(Out.DESCRIPTION) && ve.getElementAttributes().getLabel().length() == 0) {
                builder.add(new ModifyAttribute<>(ve, Keys.LABEL, outGenerator.createLabel()));
            }
        }
        modify(builder.build());
    }

    private VisualElement getActualVisualElement() {
        VisualElement ve = null;
        if (activeMouseController instanceof MouseControllerNormal)
            ve = circuit.getElementAt(getPosVector(lastMousePos.x, lastMousePos.y));
        if (activeMouseController instanceof MouseControllerMoveElement)
            ve = ((MouseControllerMoveElement) activeMouseController).getVisualElement();
        return ve;
    }

    /**
     * @return the used element library
     */
    public ElementLibrary getLibrary() {
        return library;
    }

    private void editGroup(Vector min, Vector max) {
        if (!isLocked())
            try {
                ArrayList<Key> keyList = new ArrayList<>();
                ArrayList<VisualElement> elementList = new ArrayList<>();
                HashMap<Key, Boolean> useKeyMap = new HashMap<>();
                ElementAttributes attr = new ElementAttributes();
                for (VisualElement ve : circuit.getElements())
                    if (ve.matches(min, max)) {
                        elementList.add(ve);
                        for (Key k : library.getElementType(ve.getElementName()).getAttributeList()) {
                            if (k.isGroupEditAllowed()) {
                                if (keyList.contains(k)) {
                                    if (!ve.getElementAttributes().get(k).equals(attr.get(k))) {
                                        useKeyMap.put(k, false);
                                    }
                                } else {
                                    keyList.add(k);
                                    attr.set(k, ve.getElementAttributes().get(k));
                                    useKeyMap.put(k, true);
                                }
                            }
                        }
                    }

                if (keyList.size() > 0) {
                    AttributeDialog ad = new AttributeDialog(parent, null, keyList, attr, true);
                    for (Map.Entry<Key, Boolean> u : useKeyMap.entrySet())
                        ad.getCheckBoxes().get(u.getKey()).setSelected(u.getValue());
                    ElementAttributes mod = ad.showDialog();
                    if (ad.isOkPressed()) {
                        if (mod == null) mod = attr;

                        Modifications.Builder modBuilder = new Modifications.Builder(Lang.get("mod_groupEdit"));
                        for (Key key : keyList)
                            if (ad.getCheckBoxes().get(key).isSelected()) {
                                Object newVal = mod.get(key);
                                for (VisualElement ve : elementList) {
                                    if (library.getElementType(ve.getElementName()).getAttributeList().contains(key)) {
                                        if (!ve.getElementAttributes().get(key).equals(newVal))
                                            modBuilder.add(new ModifyAttribute<>(ve, key, newVal));
                                    }
                                }
                            }
                        modify(modBuilder.build());
                    }
                }

            } catch (ElementNotFoundException e) {
                // Do nothing if an element is not in library
            }
    }

    private final class PlusMinusAction extends ToolTipAction {
        private final int delta;

        private PlusMinusAction(int delta) {
            super("plusMinus");
            this.delta = delta;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isLocked()) {
                VisualElement ve = getActualVisualElement();
                if (ve != null) {
                    try {
                        if (library.getElementType(ve.getElementName()).hasAttribute(Keys.INPUT_COUNT)) {
                            int number = ve.getElementAttributes().get(Keys.INPUT_COUNT) + delta;
                            if (number >= Keys.INPUT_COUNT.getMin() && number <= Keys.INPUT_COUNT.getMax())
                                modify(new ModifyAttribute<>(ve, Keys.INPUT_COUNT, number));
                        }
                    } catch (ElementNotFoundException e1) {
                        // do nothing on error
                    }
                }
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
            return Math.abs(d.x) > DRAG_DISTANCE || Math.abs(d.y) > DRAG_DISTANCE;
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
            lastMousePos = new Vector(e.getX(), e.getY());
            if (wasMoved(e) || isMoved) {
                isMoved = true;
                if (!activeMouseController.dragged(e)) {
                    Vector newPos = new Vector(e.getX(), e.getY());
                    Vector delta = newPos.sub(pos);
                    double s = transform.getScaleX();
                    transform.translate(delta.x / s, delta.y / s);
                    pos = newPos;
                    isManualScale = true;
                    repaintNeeded();
                }
            }
        }

    }

    //MouseController can not be final because its overridden. Maybe checkstyle has a bug?
    //CHECKSTYLE.OFF: FinalClass
    private class MouseController {
        private final Cursor mouseCursor;

        private MouseController(Cursor mouseCursor) {
            this.mouseCursor = mouseCursor;
        }

        void activate() {
            if (activeMouseController != null && activeMouseController != this)
                activeMouseController.deactivate();
            activeMouseController = this;
            deleteAction.setEnabled(false);
            copyAction.setEnabled(false);
            cutAction.setEnabled(false);
            rotateAction.setEnabled(false);
            setCursor(mouseCursor);
            repaintNeeded();
        }

        void deactivate() {
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

        public void escapePressed() {
        }
    }


    private VisualElement getVisualElement(Vector pos, boolean includeText) {
        VisualElement vp = null;
        List<VisualElement> list = circuit.getElementListAt(pos, includeText);
        if (list.size() == 1)
            vp = list.get(0);
        else if (list.size() > 1) {
            ItemPicker<VisualElement> picker = new ItemPicker<>(parent, list);
            vp = picker.select();
        }
        return vp;
    }

    //CHECKSTYLE.ON: FinalClass

    private final class MouseControllerNormal extends MouseController {
        private Vector pos;
        private MouseEvent downButton;

        private MouseControllerNormal(Cursor cursor) {
            super(cursor);
        }

        @Override
        void clicked(MouseEvent e) {
            Vector pos = getPosVector(e);

            if (mouse.isSecondaryClick(e)) {
                if (!isLocked()) {
                    VisualElement vp = getVisualElement(pos, true);
                    if (vp != null)
                        editAttributes(vp, e);
                }
            } else if (mouse.isPrimaryClick(e)) {
                VisualElement vp = getVisualElement(pos, false);
                if (vp != null) {
                    if (circuit.isPinPos(raster(pos), vp) && !mouse.isClickModifier(e)) {
                        if (!isLocked()) mouseWireRect.activate(pos);
                    } else
                        mouseMoveElement.activate(vp, pos);
                } else if (!isLocked()) {
                    if (mouse.isClickModifier(e)) {
                        Wire wire = circuit.getWireAt(pos, SIZE2);
                        if (wire != null)
                            mouseMoveWire.activate(wire, pos);
                    } else if (!focusWasLost)
                        mouseWireRect.activate(pos);
                }
            }
            focusWasLost = false;
        }

        @Override
        void deactivate() {
            removeHighLighted();
        }

        @Override
        void pressed(MouseEvent e) {
            downButton = e;
            pos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (mouse.isPrimaryClick(downButton)) {
                mouseSelect.activate(pos, getPosVector(e));
                return true;
            }
            return !mouse.isSecondaryClick(downButton);
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
            deleteAction.setEnabled(true);
            rotateAction.setEnabled(true);
        }

        @Override
        void moved(MouseEvent e) {
            updateMousePos(getPosVector(e));
        }

        void updateMousePos(Vector pos) {
            if (delta == null) {
                GraphicMinMax minMax = element.getMinMax(false);
                delta = element.getPos().sub(minMax.getMax());
            }
            element.setPos(raster(pos.add(delta)));
            repaint();
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        public void drawTo(Graphic gr) {
            if (delta != null)
                element.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isPrimaryClick(e) && !isLocked())
                modify(new ModifyInsertElement(element));
            mouseNormal.activate();
            focusWasLost = false;
        }

        @Override
        public void rotate() {
            element.rotate();
            repaint();
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }

    }

    private final class MouseControllerMoveElement extends MouseController {
        private VisualElement visualElement;
        private Vector delta;
        private Vector initialPos;
        private int initialRot;
        private boolean normalEnd;

        private MouseControllerMoveElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement visualElement, Vector pos) {
            super.activate();
            this.visualElement = visualElement;
            initialPos = visualElement.getPos();
            initialRot = visualElement.getRotate();
            delta = initialPos.sub(pos);
            deleteAction.setEnabled(true);
            rotateAction.setEnabled(true);
            copyAction.setEnabled(true);
            normalEnd = false;
            repaintNeeded();
        }

        @Override
        void clicked(MouseEvent e) {
            if (!isLocked()) {
                visualElement.setPos(raster(visualElement.getPos()));
                if (!visualElement.getPos().equals(initialPos)
                        || visualElement.getRotate() != initialRot)
                    addModificationAlreadyMade(new ModifyMoveAndRotElement(visualElement, initialPos));
                normalEnd = true;
            }
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            if (!isLocked()) {
                Vector pos = getPosVector(e);
                visualElement.setPos(raster(pos.add(delta)));
                circuit.modified();
                repaintNeeded();
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            visualElement.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void delete() {
            if (!isLocked()) {
                circuit.delete(visualElement);
                addModificationAlreadyMade(new ModifyDeleteElement(visualElement, initialPos));
                mouseNormal.activate();
                isManualScale = true;
            }
        }

        @Override
        public void rotate() {
            if (!isLocked()) {
                visualElement.rotate();
                circuit.modified();
                repaintNeeded();
            }
        }

        @Override
        public void escapePressed() {
            if (!isLocked()) {
                visualElement.setPos(raster(initialPos));
                visualElement.setRotation(initialRot);
            }
            mouseNormal.activate();
        }

        @Override
        void deactivate() {
            if (!normalEnd && !isLocked()) {
                visualElement.setPos(raster(initialPos));
                visualElement.setRotation(initialRot);
            }
        }

        public VisualElement getVisualElement() {
            return visualElement;
        }
    }

    private final class MouseControllerMoveWire extends MouseController {
        private Wire wire;
        private Vector pos;
        private Vector initialPos;
        private Vector initialWirePos;
        private boolean isMoved = false;

        private MouseControllerMoveWire(Cursor cursor) {
            super(cursor);
        }

        private void activate(Wire wire, Vector pos) {
            super.activate();
            this.wire = wire;
            this.pos = raster(pos);
            this.initialWirePos = wire.getPos();
            this.initialPos = this.pos;
            deleteAction.setEnabled(true);
            removeHighLighted();
            repaintNeeded();
        }

        @Override
        void clicked(MouseEvent e) {
            if (isMoved) {
                removeHighLighted();
                addModificationAlreadyMade(new ModifyMoveWire(wire, initialWirePos));
                circuit.elementsMoved();
                isMoved = false;
            }
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            Vector pos = raster(getPosVector(e));
            final Vector delta = pos.sub(this.pos);
            if (!delta.isZero()) {
                isMoved = true;
                wire.move(delta);
                wire.noDot();
                isManualScale = true;
                circuit.modified();
                repaintNeeded();
            }
            this.pos = pos;
        }

        @Override
        public void delete() {
            circuit.delete(wire);
            addModificationAlreadyMade(new ModifyDeleteWire(wire, initialWirePos));
            mouseNormal.activate();
            isManualScale = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            // ensure that highlighted wire is visible by drawing it on top of other drawings.
            wire.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void escapePressed() {
            deactivate();
            mouseNormal.activate();
        }

        @Override
        void deactivate() {
            if (isMoved) {
                wire.move(initialPos.sub(pos));
                isMoved = false;
            }
            removeHighLighted();
        }
    }


    private final class MouseControllerWireDiag extends MouseController {
        private Wire wire;

        private MouseControllerWireDiag(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector startPos, Vector endPos) {
            super.activate();
            wire = new Wire(raster(startPos), raster(endPos));
        }

        @Override
        void moved(MouseEvent e) {
            wire.setP2(raster(getPosVector(e)));
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isClickModifier(e)) {
                Vector pos = raster(getPosVector(e));
                Wire wire = circuit.getWireAt(pos, SIZE2);
                if (wire != null)
                    mouseMoveWire.activate(wire, pos);
            } else {
                if (mouse.isSecondaryClick(e))
                    mouseNormal.activate();
                else if (mouse.isPrimaryClick(e)) {
                    modify(new ModifyInsertWire(wire).checkIfLenZero());
                    if (circuit.isPinPos(wire.p2))
                        mouseNormal.activate();
                    else
                        mouseWireRect.activate(wire.p2);
                }
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            wire.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }

        private void rectangularWire() {
            mouseWireRect.activate(wire.p1, wire.p2);
        }
    }

    private final class MouseControllerWireRect extends MouseController {
        private Wire wire1;
        private Wire wire2;
        private boolean selectionMade;
        private boolean firstHorizontal;
        private Vector initialPos;
        private Vector lastPosition;

        private MouseControllerWireRect(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector startPos) {
            startPos = raster(startPos);
            activate(startPos, startPos);
            selectionMade = false;
        }

        private void activate(Vector startPos, Vector endPos) {
            super.activate();
            initialPos = raster(startPos);
            wire1 = new Wire(startPos, endPos);
            wire2 = new Wire(startPos, endPos);
            selectionMade = true;
            lastPosition = endPos;
            setWires();
        }

        @Override
        void moved(MouseEvent e) {
            lastPosition = raster(getPosVector(e));
            if (!selectionMade) {
                Vector delta = lastPosition.sub(initialPos);
                boolean dx = Math.abs(delta.x) > DRAG_DISTANCE;
                boolean dy = Math.abs(delta.y) > DRAG_DISTANCE;
                if (dx || dy) {
                    firstHorizontal = dx;
                    selectionMade = true;
                }
            }
            setWires();
        }

        private void setWires() {
            Vector pm;
            if (firstHorizontal)
                pm = new Vector(lastPosition.x, wire1.p1.y);
            else
                pm = new Vector(wire1.p1.x, lastPosition.y);
            wire1.setP2(pm);
            wire2.setP1(pm);
            wire2.setP2(lastPosition);
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isClickModifier(e)) {
                Vector pos = raster(getPosVector(e));
                Wire wire = circuit.getWireAt(pos, SIZE2);
                if (wire != null)
                    mouseMoveWire.activate(wire, pos);
            } else {
                if (mouse.isSecondaryClick(e))
                    mouseNormal.activate();
                else if (mouse.isPrimaryClick(e)) {
                    modify(new Modifications.Builder(Lang.get("mod_insertWire"))
                            .add(new ModifyInsertWire(wire1).checkIfLenZero())
                            .add(new ModifyInsertWire(wire2).checkIfLenZero())
                            .build());
                    if (circuit.isPinPos(wire2.p2))
                        mouseNormal.activate();
                    else {
                        initialPos = wire2.p2;
                        selectionMade = false;
                        wire1 = new Wire(wire2.p2, wire2.p2);
                        wire2 = new Wire(wire2.p2, wire2.p2);
                    }
                }
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            wire1.drawTo(gr, Style.HIGHLIGHT);
            wire2.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }

        void diagonalWire() {
            mouseWireDiag.activate(initialPos, wire2.p2);
            repaint();
        }

        private void flipWire() {
            selectionMade = true;
            firstHorizontal = !firstHorizontal;
            setWires();
        }
    }

    private final class MouseControllerWireSplit extends MouseController {
        private Wire wire1;
        private Wire wire2;
        private Vector newPosition;
        private Wire origWire;
        private boolean splitDone = false;

        private MouseControllerWireSplit(Cursor cursor) {
            super(cursor);
        }

        private void activate(Wire w, Vector startPos) {
            super.activate();
            startPos = raster(startPos);
            origWire = new Wire(w);
            wire1 = w;
            wire1.setP2(startPos);
            wire1.noDot();
            wire2 = new Wire(startPos, origWire.p2);
            circuit.getWires().add(wire2);
        }

        @Override
        void moved(MouseEvent e) {
            Vector p = raster(getPosVector(e));
            if (!p.equals(newPosition)) {
                newPosition = p;
                wire1.setP2(newPosition);
                wire2.setP1(newPosition);
                circuitHasChanged();
                repaint();
            }
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isPrimaryClick(e)) {
                addModificationAlreadyMade(
                        new ModifySplitWire(origWire, newPosition));
                splitDone = true;
                circuit.elementsMoved();
                mouseNormal.activate();
            } else if (mouse.isSecondaryClick(e))
                escapePressed();
        }

        @Override
        void deactivate() {
            if (!splitDone) {
                wire1.setP2(origWire.p2);
                circuit.getWires().remove(wire2);
                circuitHasChanged();
            }
        }

        @Override
        public void escapePressed() {
            wire1.setP2(origWire.p2);
            circuit.getWires().remove(wire2);
            circuitHasChanged();
            mouseNormal.activate();
        }

        @Override
        public void drawTo(Graphic gr) {
            wire1.drawTo(gr, Style.HIGHLIGHT);
            wire2.drawTo(gr, Style.HIGHLIGHT);
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
            deleteAction.setEnabled(true);
            copyAction.setEnabled(true);
            cutAction.setEnabled(true);
            rotateAction.setEnabled(true);
            wasReleased = false;
            updateHighlighting();
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isPrimaryClick(e)) {
                mouseNormal.activate();
                removeHighLighted();
            } else if (mouse.isSecondaryClick(e)) {
                editGroup(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
                mouseNormal.activate();
                removeHighLighted();
            }
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
                if (!isLocked())
                    mouseMoveSelected.activate(corner1, corner2, getPosVector(e));
            } else {
                corner2 = getPosVector(e);
                if (mouse.isClickModifier(e)) {
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

                updateHighlighting();
            }
            return true;
        }

        private void updateHighlighting() {
            ArrayList<Drawable> elements = circuit.getElementsToHighlight(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
            removeHighLighted();
            if (elements != null)
                addHighLighted(elements);

            repaint();
        }

        public void release() {
            this.wasReleased = true;
        }

        @Override
        public void drawTo(Graphic gr) {
            Vector p1 = new Vector(corner1.x, corner2.y);
            Vector p2 = new Vector(corner2.x, corner1.y);
            gr.drawLine(corner1, p1, Style.DASH);
            gr.drawLine(p1, corner2, Style.DASH);
            gr.drawLine(p2, corner2, Style.DASH);
            gr.drawLine(corner1, p2, Style.DASH);
        }

        @Override
        public void delete() {
            if (!isLocked()) {
                modify(new ModifyDeleteRect(Vector.min(corner1, corner2), Vector.max(corner1, corner2)));
                mouseNormal.activate();
                isManualScale = true;
            }
        }

        public void rotate() {
            if (!isLocked()) {
                mouseMoveSelected.activate(corner1, corner2, lastMousePos);
                mouseMoveSelected.rotate();
            }
        }

        @Override
        public void escapePressed() {
            removeHighLighted();
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveSelected extends MouseController {
        private ArrayList<Movable> elements;
        private Vector lastPos;
        private Vector center;
        private Vector accumulatedDelta;
        private int accumulatedRotate;
        private boolean hasChangedCircuit;
        private Vector min;
        private Vector max;

        private MouseControllerMoveSelected(Cursor cursor) {
            super(cursor);
        }

        private void activate(Vector corner1, Vector corner2, Vector pos) {
            super.activate();
            rotateAction.setEnabled(true);
            lastPos = pos;
            center = raster(corner1.add(corner2).div(2));
            hasChangedCircuit = false;
            accumulatedDelta = new Vector(0, 0);
            accumulatedRotate = 0;
            min = Vector.min(corner1, corner2);
            max = Vector.max(corner1, corner2);
            elements = circuit.getElementsToMove(min, max);
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
                    for (Movable m : elements)
                        m.move(delta);
                    accumulatedDelta = accumulatedDelta.add(delta);
                    hasChangedCircuit = true;

                    repaintNeeded();
                    lastPos = lastPos.add(delta);
                    center = center.add(delta);
                }
            }
            return true;
        }

        @Override
        void released(MouseEvent e) {
            if (hasChangedCircuit) {
                hasChangedCircuit = false;
                addModificationAlreadyMade(new ModifyMoveSelected(min, max, accumulatedDelta, accumulatedRotate, center));
                circuit.elementsMoved();
            }
            removeHighLighted();
            mouseNormal.activate();
        }

        @Override
        void deactivate() {
            if (hasChangedCircuit) {
                hasChangedCircuit = false;
                new ModifyMoveSelected(min, max, accumulatedDelta, accumulatedRotate, center).revert(elements);
            }
        }

        @Override
        public void escapePressed() {
            deactivate();
            removeHighLighted();
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            ModifyMoveSelected.rotateElements(elements, center);
            repaintNeeded();
            accumulatedRotate++;
            hasChangedCircuit = true;
        }
    }

    private final class MouseControllerInsertCopied extends MouseController {
        private ArrayList<Movable> elements;
        private Vector lastPos;

        private MouseControllerInsertCopied(Cursor cursor) {
            super(cursor);
        }

        private void activate(ArrayList<Movable> elements, Vector pos) {
            super.activate();
            this.elements = elements;
            lastPos = pos;

            Vector max = null;
            for (Movable m : elements)
                if (m instanceof VisualElement) {
                    GraphicMinMax mm = ((VisualElement) m).getMinMax(false);
                    if (max == null)
                        max = mm.getMax();
                    else
                        max = Vector.max(max, mm.getMax());
                }

            if (max != null) {
                Vector delta = CircuitComponent.raster(lastPos.sub(max));
                for (Movable m : elements)
                    m.move(delta);
            }

            deleteAction.setEnabled(true);
            rotateAction.setEnabled(true);
        }

        @Override
        void moved(MouseEvent e) {
            if (elements != null) {
                Vector pos = getPosVector(e);
                Vector delta = raster(pos.sub(lastPos));

                if (delta.x != 0 || delta.y != 0) {
                    for (Movable m : elements)
                        m.move(delta);

                    repaint();
                    lastPos = lastPos.add(delta);
                }
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            if (elements != null)
                for (Movable m : elements)
                    if (m instanceof Drawable)
                        ((Drawable) m).drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void delete() {
            mouseNormal.activate();
        }

        @Override
        void clicked(MouseEvent e) {
            if (elements != null && mouse.isPrimaryClick(e)) {
                Modifications.Builder builder = new Modifications.Builder(Lang.get("mod_insertCopied"));
                for (Movable m : elements) {
                    if (m instanceof Wire)
                        builder.add(new ModifyInsertWire((Wire) m));
                    if (m instanceof VisualElement)
                        builder.add(new ModifyInsertElement((VisualElement) m));
                }
                modify(builder.build());
            }
            mouseNormal.activate();
            focusWasLost = false;
        }

        @Override
        public void rotate() {
            ModifyMoveSelected.rotateElements(elements, raster(lastPos));
            circuit.modified();
            repaintNeeded();
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }


    private interface Actor {
        boolean interact(CircuitComponent cc, Point p, Vector posInComponent, SyncAccess modelSync);
    }

    private final class MouseControllerRun extends MouseController {

        private boolean dragHandled;

        private MouseControllerRun(Cursor cursor) {
            super(cursor);
        }

        @Override
        void pressed(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null) {
                interact(e, ve::elementPressed);
                dragHandled = true;
            } else
                dragHandled = false;
        }

        private VisualElement getInteractiveElementAt(MouseEvent e) {
            List<VisualElement> elementList = circuit.getElementListAt(getPosVector(e), false);
            for (VisualElement ve : elementList) {
                if (ve.isInteractive())
                    return ve;
            }
            return null;
        }

        @Override
        void released(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null)
                interact(e, ve::elementReleased);
        }

        @Override
        void clicked(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null)
                interact(e, ve::elementClicked);
        }

        @Override
        boolean dragged(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null)
                interact(e, ve::elementDragged);
            return dragHandled;
        }

        private void interact(MouseEvent e, Actor actor) {
            Point p = new Point(e.getX(), e.getY());
            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
            boolean modelHasChanged = actor.interact(CircuitComponent.this, p, getPosVector(e), modelSync);
            if (modelHasChanged) {
                modelHasChanged();
            } else
                repaintNeeded();
        }
    }

    /**
     * call this method if the model has changed manually
     */
    public void modelHasChanged() {
        if (manualChangeObserver != null)
            manualChangeObserver.hasChanged();
    }

    /**
     * Activate a wizard
     *
     * @param wizardNotification the wizard notification
     */
    public void activateWizard(WizardNotification wizardNotification) {
        mouseNormal.activate();
        circuit.clearState();
        new MouseControllerWizard(wizardNotification).activate();
    }

    /**
     * Deactivate a wizard
     */
    public void deactivateWizard() {
        if (activeMouseController instanceof MouseControllerWizard) {
            MouseControllerWizard mcw = (MouseControllerWizard) activeMouseController;
            mcw.wizardNotification.closed();
        }
        mouseNormal.activate();
    }

    private final class MouseControllerWizard extends MouseController {

        private final WizardNotification wizardNotification;

        private MouseControllerWizard(WizardNotification wizardNotification) {
            super(new Cursor(Cursor.CROSSHAIR_CURSOR));
            this.wizardNotification = wizardNotification;
        }

        @Override
        void clicked(MouseEvent e) {
            Vector pos = getPosVector(e);
            VisualElement vp = getVisualElement(pos, true);
            if (vp != null)
                wizardNotification.notify(vp);
        }

        @Override
        public void escapePressed() {
            wizardNotification.closed();
            mouseNormal.activate();
        }
    }

    /**
     * Interface to interact with wizards
     */
    public interface WizardNotification {
        /**
         * Called if an element is clicked
         *
         * @param clicked the element clicked
         */
        void notify(VisualElement clicked);

        /**
         * Called if the wizard is to close
         */
        void closed();
    }

}
