/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.switching.Switch;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.library.*;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.draw.shapes.Drawable;
import de.neemann.digital.draw.shapes.InputShape;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.Settings;
import de.neemann.digital.gui.components.modification.*;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.undo.*;
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
import java.util.List;
import java.util.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * Component which shows the circuit.
 * ToDo: refactoring of repaint logic. Its to complex now.
 * ToDo: class is to large, move the MouseController classes to their own package
 */
public class CircuitComponent extends JComponent implements ChangedListener, LibraryListener {
    /**
     * The delete icon, also used from {@link de.neemann.digital.gui.components.terminal.TerminalDialog}
     */
    public static final Icon ICON_DELETE = IconCreator.create("delete.png");
    private static final Icon ICON_UNDO = IconCreator.create("edit-undo.png");
    private static final Icon ICON_REDO = IconCreator.create("edit-redo.png");
    private static final ArrayList<Key> ATTR_LIST = new ArrayList<>();

    static {
        ATTR_LIST.add(Keys.LABEL);
        ATTR_LIST.add(Keys.WIDTH);
        ATTR_LIST.add(Keys.SHAPE_TYPE);
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
        ATTR_LIST.add(Keys.IS_GENERIC);
    }

    /**
     * @return returns the list of circuit attributes
     */
    public static ArrayList<Key> getAttrList() {
        return ATTR_LIST;
    }

    private static final String DEL_ACTION = "myDelAction";
    private static final int MOUSE_BORDER_SMALL = 10;
    private static final int MOUSE_BORDER_LARGE = 50;

    private static final int DRAG_DISTANCE = (int) (SIZE2 * Screen.getInstance().getScaling());

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
    private final UndoManager<Circuit> undoManager;

    private MouseController activeMouseController;
    private AffineTransform transform = new AffineTransform();
    private Vector lastMousePos;
    private SyncAccess modelSync = SyncAccess.NOSYNC;
    private boolean isManualScale;
    private boolean graphicHasChangedFlag = true;
    private boolean hadFocusAtClick = true;
    private boolean lockMessageShown = false;
    private boolean antiAlias = true;

    private Style highLightStyle = Style.HIGHLIGHT;
    private Mouse mouse = Mouse.getMouse();
    private Circuit shallowCopy;
    private CircuitScrollPanel circuitScrollPanel;
    private TutorialListener tutorialListener;
    private boolean toolTipHighlighted = false;
    private NetList toolTipNetList;
    private String lastUsedTunnelName;

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
                    Wire w = getCircuit().getWireAt(pos, SIZE2);
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
            if (circuitScrollPanel != null)
                circuitScrollPanel.transformChanged(transform);
            graphicHasChanged();
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (!isManualScale)
                    fitCircuit();
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

        undoManager = new UndoManager<>(new Circuit());
        addListener(this);

        MouseDispatcher dispatcher = new MouseDispatcher();
        addMouseMotionListener(dispatcher);
        addMouseListener(dispatcher);

        enableFavoritePositions();

        mouseNormal.activate();

        if (parent != null) {
            parent.addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent e) {
                    if (!(activeMouseController instanceof MouseControllerWizard || activeMouseController == mouseSelect))
                        activeMouseController.escapePressed();
                }
            });
        }

        setToolTipText("");
    }

    private void enableFavoritePositions() {
        for (int j = 0; j <= 9; j++) {
            final int i = j;
            final Key<TransformHolder> key = new Key<>("view" + i, TransformHolder::new);
            new ToolTipAction("CTRL+" + i) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ElementAttributes attr = new ElementAttributes(getCircuit().getAttributes());
                    attr.set(key, new TransformHolder(transform));
                    modify(new ModifyCircuitAttributes(attr));
                }
            }.setAcceleratorCTRLplus((char) ('0' + i)).enableAcceleratorIn(this);
            new ToolTipAction("" + i) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    TransformHolder transformHolder = getCircuit().getAttributes().get(key);
                    if (!transformHolder.isIdentity()) {
                        transform = transformHolder.createAffineTransform();
                        isManualScale = true;
                        graphicHasChanged();
                        if (circuitScrollPanel != null)
                            circuitScrollPanel.transformChanged(transform);
                    }
                }
            }.setAccelerator(KeyStroke.getKeyStroke((char) ('0' + i), 0)).enableAcceleratorIn(this);
        }
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
                    getCircuit().drawTo(gr);
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

        new ToolTipAction("insertTunnel") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (activeMouseController == mouseNormal) {
                    VisualElement tunnel =
                            new VisualElement(Tunnel.DESCRIPTION.getName())
                                    .setShapeFactory(shapeFactory);
                    setPartToInsert(tunnel);
                }
            }
        }.setAccelerator("T").enableAcceleratorIn(this);

        ToolTipAction plus = new PlusMinusAction(1).setAccelerator("PLUS").enableAcceleratorIn(this);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), plus);

        ToolTipAction minus = new PlusMinusAction(-1).setAccelerator("MINUS").enableAcceleratorIn(this);
        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), minus);

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
                            ArrayList<Movable> elements = CircuitTransferable.createList(data, shapeFactory);
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
                        ArrayList<Movable> elements = getCircuit().copyElementsToMove(min, max, shapeFactory);
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
            elements = getCircuit().copyElementsToMove(Vector.min(mcs.corner1, mcs.corner2), Vector.max(mcs.corner1, mcs.corner2), shapeFactory);
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
        ElementAttributes modifiedAttributes =
                new AttributeDialog(parent, ATTR_LIST, getCircuit().getAttributes())
                        .setDialogTitle(Lang.get("menu_editAttributes"))
                        .showDialog();
        if (modifiedAttributes != null)
            modify(new ModifyCircuitAttributes(modifiedAttributes));
    }

    /**
     * Apply a modification
     *
     * @param modification the modification
     */
    public void modify(Modification<Circuit> modification) {
        try {
            if (modification != null) {
                toolTipNetList = null;
                undoManager.apply(modification);
                if (tutorialListener != null)
                    tutorialListener.modified(modification);
                if (circuitScrollPanel != null)
                    circuitScrollPanel.sizeChanged();
            }
        } catch (ModifyException e) {
            throw new RuntimeException("internal error in modify", e);
        }
    }

    /**
     * invalidates the image buffer and calls repaint();
     */
    public void graphicHasChanged() {
        graphicHasChangedFlag = true;
        repaint();
    }

    /**
     * undo last action
     */
    private void undo() {
        if (activeMouseController != mouseNormal)
            activeMouseController.escapePressed();
        else {
            if (!isLocked() && undoManager.undoAvailable()) {
                try {
                    undoManager.undo();
                } catch (ModifyException e) {
                    throw new RuntimeException("internal error in undo", e);
                }
            }
        }
    }

    private String getUndoToolTip() {
        if (undoManager.undoAvailable())
            return Lang.get("mod_undo_N", undoManager.getUndoModification().toString());
        else
            return Lang.get("menu_undo_tt");
    }

    /**
     * redo last undo
     */
    private void redo() {
        if (activeMouseController != mouseNormal)
            activeMouseController.escapePressed();
        else {
            if (!isLocked() && undoManager.redoAvailable()) {
                try {
                    undoManager.redo();
                } catch (ModifyException e) {
                    throw new RuntimeException("internal error in redo", e);
                }
            }
        }
    }

    private String getRedoToolTip() {
        if (undoManager.redoAvailable())
            return Lang.get("mod_redo_N", undoManager.getRedoModification().toString());
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
        getCircuit().save(filename);
        try {
            undoManager.applyWithoutHistory(circuit -> circuit.setOrigin(filename));
        } catch (ModifyException e) {
            throw new RuntimeException("internal error in save", e);
        }
        undoManager.saved();
    }

    /**
     * @return the main frame
     */
    public Main getMain() {
        return parent;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (toolTipHighlighted) {
            toolTipHighlighted = false;
            removeHighLighted();
        }

        Vector pos = getPosVector(event);
        VisualElement ve = getCircuit().getElementAt(pos);
        if (ve != null) {
            Pin p = ve.getPinAt(raster(pos));
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

        Wire w = getCircuit().getWireAt(pos, SIZE2);
        if (w != null) {
            ObservableValue v = w.getValue();
            if (v != null)
                return v.getValueString();
            else {
                if (Settings.getInstance().get(Keys.SETTINGS_WIRETOOLTIP))
                    if (highLighted == null || highLighted.isEmpty() || toolTipHighlighted) {
                        try {
                            if (toolTipNetList == null)
                                toolTipNetList = new NetList(getCircuit());
                            Net n = toolTipNetList.getNetOfPos(w.p1);
                            if (n != null) {
                                removeHighLighted();
                                addHighLighted(n.getWires());
                                toolTipHighlighted = true;
                            }
                        } catch (PinException e) {
                            e.printStackTrace();
                        }
                    }
            }
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
     * Sets the edit mode and resets the circuit
     *
     * @param runMode   true if running, false if editing
     * @param modelSync used to access the running model
     */
    public void setModeAndReset(boolean runMode, SyncAccess modelSync) {
        this.modelSync = modelSync;
        if (runMode) {
            redoAction.setEnabled(false);
            undoAction.setEnabled(false);
            mouseRun.activate();
        } else {
            enableUndoRedo();
            mouseNormal.activate();
            getCircuit().clearState();
        }
        requestFocusInWindow();

        if (tutorialListener != null)
            tutorialListener.modified(null);
    }

    private void enableUndoRedo() {
        redoAction.setEnabled(undoManager.redoAvailable());
        undoAction.setEnabled(undoManager.undoAvailable());
    }

    /**
     * @return the highlighted elements
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
        if (drawable != null) {
            highLighted.add(drawable);
            graphicHasChanged();
        }
    }

    /**
     * Add a list of drawables to high light
     *
     * @param drawables the list of drawables
     */
    public void addHighLighted(Collection<? extends Drawable> drawables) {
        if (drawables != null) {
            highLighted.addAll(drawables);
            graphicHasChanged();
        }
    }

    /**
     * Adds all the wires representing the given value to the highlighted list
     *
     * @param values the value
     */
    public void addHighLightedWires(ImmutableList<ObservableValue> values) {
        if (values == null) return;

        HashSet<ObservableValue> ov = new HashSet<>(values);
        for (Wire w : getCircuit().getWires())
            if (ov.contains(w.getValue()))
                addHighLighted(w);
    }

    /**
     * remove all highlighted elements
     */
    public void removeHighLighted() {
        if (!highLighted.isEmpty()) {
            highLighted.clear();
            highLightStyle = Style.HIGHLIGHT;
            graphicHasChanged();
        }

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
        if (element.equalsDescription(Tunnel.DESCRIPTION)) {
            if (lastUsedTunnelName != null) {
                CopiedElementLabelRenamer.LabelInstance li =
                        CopiedElementLabelRenamer.LabelInstance.
                                create(Tunnel.DESCRIPTION.getName(), lastUsedTunnelName);
                if (li != null) {
                    lastUsedTunnelName = li.getLabel(1);
                }
                element.setAttribute(Keys.NETNAME, lastUsedTunnelName);
            }
        }

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
        graphicHasChanged();
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
        elements = new CopiedElementLabelRenamer(getCircuit(), elements).rename();
        mouseInsertList.activate(elements, pos);
        graphicHasChanged();
    }


    private BufferedImage buffer;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        boolean newBufferRequired = buffer == null
                || getWidth() != buffer.getWidth()
                || getHeight() != buffer.getHeight();

        if (newBufferRequired && !isManualScale)
            fitCircuit();

        final double scaleX = transform.getScaleX();
        if (graphicHasChangedFlag || newBufferRequired) {

            if (newBufferRequired)
                buffer = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(getWidth(), getHeight());

            Graphics2D gr2 = buffer.createGraphics();
            enableAntiAlias(gr2);
            gr2.setColor(ColorScheme.getSelected().getColor(ColorKey.BACKGROUND));
            gr2.fillRect(0, 0, getWidth(), getHeight());

            if (scaleX > 0.3 && Settings.getInstance().get(Keys.SETTINGS_GRID))
                drawGrid(gr2);

            gr2.transform(transform);

            GraphicSwing gr = new GraphicSwing(gr2, (int) (2 / scaleX));

            long time = System.currentTimeMillis();
            if (shallowCopy != null)
                shallowCopy.drawTo(gr, highLighted, highLightStyle, modelSync);
            else
                getCircuit().drawTo(gr, highLighted, highLightStyle, modelSync);
            time = System.currentTimeMillis() - time;

            if (time > 500) antiAlias = false;
            if (time < 50) antiAlias = true;

//            System.out.println("repaint: " + time + "ms");

            graphicHasChangedFlag = false;
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

        gr2.setColor(ColorScheme.getSelected().getColor(ColorKey.GRID));
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
    public void hasChanged() {
        graphicHasChanged();
        enableUndoRedo();
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
        return undoManager.getActual();
    }

    /**
     * Sets a circuit to this component
     *
     * @param circuit the circuit
     */
    public void setCircuit(Circuit circuit) {
        undoManager.setInitial(circuit);
        undoAction.setEnabled(false);
        redoAction.setEnabled(false);

        toolTipNetList = null;

        if (circuitScrollPanel != null)
            circuitScrollPanel.sizeChanged();
        fitCircuit();
        setModeAndReset(false, SyncAccess.NOSYNC);
    }

    /**
     * maximizes the circuit shown
     */
    public void fitCircuit() {
        GraphicMinMax gr = new GraphicMinMax();
        getCircuit().drawTo(gr);

        AffineTransform newTrans = new AffineTransform();
        if (gr.getMin() != null && getWidth() != 0 && getHeight() != 0) {
            Vector delta = gr.getMax().sub(gr.getMin());
            int pad = circuitScrollPanel.getBarWidth();
            double sx = ((double) getWidth() - pad) / (delta.x + SIZE * 2);
            double sy = ((double) getHeight() - pad) / (delta.y + SIZE * 2);
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
            if (circuitScrollPanel != null)
                circuitScrollPanel.transformChanged(transform);
            graphicHasChanged();
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
        if (circuitScrollPanel != null)
            circuitScrollPanel.transformChanged(transform);
        graphicHasChanged();
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
        if (circuitScrollPanel != null)
            circuitScrollPanel.transformChanged(transform);
        graphicHasChanged();
    }

    /**
     * Translates the circuit.
     *
     * @param x x position
     */
    void translateCircuitToX(double x) {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        matrix[4] = x;
        transform = new AffineTransform(matrix);
        isManualScale = true;
        if (circuitScrollPanel != null)
            circuitScrollPanel.transformChanged(transform);
        graphicHasChanged();
    }

    /**
     * Translates the circuit.
     *
     * @param y y position
     */
    void translateCircuitToY(double y) {
        double[] matrix = new double[6];
        transform.getMatrix(matrix);
        matrix[5] = y;
        transform = new AffineTransform(matrix);
        isManualScale = true;
        if (circuitScrollPanel != null)
            circuitScrollPanel.transformChanged(transform);
        graphicHasChanged();
    }

    private void editAttributes(VisualElement element, MouseEvent e) {
        try {
            ArrayList<Key> list = getAttributeList(element);
            if (list.size() > 0) {
                ElementTypeDescription elementType = library.getElementType(element.getElementName());

                if (elementType instanceof ElementTypeDescriptionCustom) {
                    ElementTypeDescriptionCustom customDescr = (ElementTypeDescriptionCustom) elementType;
                    if (customDescr.isGeneric()) {
                        if (element.getElementAttributes().get(Keys.GENERIC).isEmpty()) {
                            try {
                                element.getElementAttributes().set(Keys.GENERIC, customDescr.getDeclarationDefault());
                            } catch (NodeException ex) {
                                new ErrorMessage(Lang.get("msg_errParsingGenerics")).addCause(ex).show(CircuitComponent.this);
                            }
                        }
                    }
                }

                Point p = new Point(e.getX(), e.getY());
                SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
                AttributeDialog attributeDialog = new AttributeDialog(parent, p, list, element.getElementAttributes())
                        .setDialogTitle(elementType.getTranslatedName())
                        .setVisualElement(element);
                if (elementType instanceof ElementTypeDescriptionCustom) {
                    attributeDialog.addButton(Lang.get("attr_openCircuitLabel"), new ToolTipAction(Lang.get("attr_openCircuit")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            attributeDialog.dispose();
                            new Main.MainBuilder()
                                    .setParent(parent)
                                    .setFileToOpen(((ElementTypeDescriptionCustom) elementType).getFile())
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
                            attributeDialog.dispose();
                            new ElementHelpDialog(
                                    attributeDialog.getDialogParent(),
                                    elementType,
                                    element.getElementAttributes(),
                                    getCircuit().getAttributes().get(Keys.IS_GENERIC)).setVisible(true);
                        } catch (PinException | NodeException e1) {
                            new ErrorMessage(Lang.get("msg_creatingHelp")).addCause(e1).show(CircuitComponent.this);
                        }
                    }
                }.setToolTip(Lang.get("attr_help_tt")));

                boolean locked = isLocked();
                if (isLocked())
                    attributeDialog.disableOk();

                ElementAttributes modified = attributeDialog.showDialog();
                if (elementType == Tunnel.DESCRIPTION) {
                    if (modified.contains(Keys.NETNAME))
                        lastUsedTunnelName = modified.get(Keys.NETNAME);
                }
                if (modified != null && !locked) {
                    Modification<Circuit> mod = new ModifyAttributes(element, modified);
                    modify(checkNetRename(element, modified, mod));
                }
            }
        } catch (ElementNotFoundException ex) {
            // do nothing if element not found!
        }
    }

    private Modification<Circuit> checkNetRename(VisualElement element, ElementAttributes modified, Modification<Circuit> mod) {
        String oldName = element.getElementAttributes().get(Keys.NETNAME);
        if (element.equalsDescription(Tunnel.DESCRIPTION)
                && modified.contains(Keys.NETNAME)
                && !modified.get(Keys.NETNAME).equals(oldName)
                && !oldName.isEmpty()) {

            List<VisualElement> others = getCircuit().getElements(el -> el != element
                    && el.equalsDescription(Tunnel.DESCRIPTION)
                    && el.getElementAttributes().get(Keys.NETNAME).equals(oldName));

            if (others.size() > 0) {
                String newName = modified.get(Keys.NETNAME);
                if (Settings.getInstance().get(Keys.SETTINGS_SHOW_TUNNEL_RENAME_DIALOG)) {
                    int res = JOptionPane.showConfirmDialog(this,
                            new LineBreaker().toHTML().preserveContainedLineBreaks().breakLines(Lang.get("msg_renameNet_N_OLD_NEW", others.size(), oldName, newName)),
                            Lang.get("msg_renameNet"),
                            JOptionPane.YES_NO_OPTION);
                    if (res == JOptionPane.YES_OPTION) {
                        Modifications.Builder<Circuit> b =
                                new Modifications.Builder<Circuit>(Lang.get("msg_renameNet")).add(mod);
                        for (VisualElement o : others)
                            b.add(new ModifyAttribute<>(o, Keys.NETNAME, newName));
                        return b.build();
                    }
                }
            }
        }
        return mod;
    }

    @Override
    public void libraryChanged(LibraryNode node) {
        getCircuit().clearState();
        graphicHasChangedFlag = true;
        repaint();
    }

    /**
     * @return returns true if this circuit is locked
     */
    public boolean isLocked() {
        final boolean locked = getCircuit().getAttributes().get(Keys.LOCKED_MODE);
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
            Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_actualToDefault"));
            for (VisualElement ve : getCircuit().getElements())
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
        Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_restoreAllFuses"));
        for (VisualElement ve : getCircuit().getElements())
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

        Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("menu_labelPins"));
        for (VisualElement ve : getCircuit().getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION) && ve.getElementAttributes().getLabel().length() == 0) {
                builder.add(new ModifyAttribute<>(ve, Keys.LABEL, inGenerator.createLabel()));
            } else if (ve.equalsDescription(Out.DESCRIPTION) && ve.getElementAttributes().getLabel().length() == 0) {
                builder.add(new ModifyAttribute<>(ve, Keys.LABEL, outGenerator.createLabel()));
            }
        }
        modify(builder.build());
    }

    private VisualElement getActualVisualElement() {
        if (activeMouseController instanceof MouseControllerMoveElement)
            mouseNormal.activate();

        VisualElement ve = null;
        if (activeMouseController instanceof MouseControllerNormal) {
            Vector pos = getPosVector(lastMousePos.x, lastMousePos.y);
            ve = getCircuit().getElementAt(pos);
            if (ve == null)
                ve = getCircuit().getElementAt(pos, true);
        }
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
                for (VisualElement ve : getCircuit().getElements())
                    if (ve.matches(min, max)) {
                        elementList.add(ve);
                        for (Key k : getAttributeList(ve)) {
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

                        Modifications.Builder<Circuit> modBuilder = new Modifications.Builder<>(Lang.get("mod_groupEdit"));
                        for (Key key : keyList)
                            if (ad.getCheckBoxes().get(key).isSelected()) {
                                Object newVal = mod.get(key);
                                for (VisualElement ve : elementList) {
                                    if (getAttributeList(ve).contains(key)) {
                                        if (!ve.getElementAttributes().get(key).equals(newVal))
                                            modBuilder.add(new ModifyAttribute<>(ve, key, newVal));
                                    }
                                }
                            }
                        modify(modBuilder.build());
                    }
                }

            } catch (ElementNotFoundException e) {
                // Do nothing if an element is not in the library
            }
    }

    private ArrayList<Key> getAttributeList(VisualElement ve) throws ElementNotFoundException {
        ArrayList<Key> list = library.getElementType(ve.getElementName()).getAttributeList();
        if (getCircuit().getAttributes().get(Keys.IS_GENERIC) && !list.contains(Keys.GENERIC)) {
            list = new ArrayList<>(list);
            list.add(Keys.GENERIC);
        }
        return list;
    }

    /**
     * @return true if circuit is modified
     */
    public boolean isModified() {
        return undoManager.isModified();
    }

    /**
     * Adds a listener to the circuit.
     *
     * @param listener the listener to add
     */
    public void addListener(ChangedListener listener) {
        undoManager.addListener(listener);
    }

    /**
     * @return true is component is in manual scale mode
     */
    boolean isManualScale() {
        return isManualScale;
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
                        } else if (ve.equalsDescription(Const.DESCRIPTION)) {
                            long v = ve.getElementAttributes().get(Keys.VALUE) + delta;
                            v &= Bits.mask(ve.getElementAttributes().getBits());
                            modify(new ModifyAttribute<>(ve, Keys.VALUE, v));
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
            hadFocusAtClick = hasFocus() || parent.hasMouseFocus();
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
            if (toolTipHighlighted) {
                removeHighLighted();
                toolTipHighlighted = false;
            }
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
                    // if active mouse controller does not handle the drag, move the circuit instead.
                    Vector newPos = new Vector(e.getX(), e.getY());
                    Vector delta = newPos.sub(pos);
                    double s = transform.getScaleX();
                    transform.translate(delta.x / s, delta.y / s);
                    isManualScale = true;
                    if (circuitScrollPanel != null)
                        circuitScrollPanel.transformChanged(transform);
                    pos = newPos;
                    graphicHasChanged();
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
            shallowCopy = null;
            deleteAction.setEnabled(false);
            copyAction.setEnabled(false);
            cutAction.setEnabled(false);
            rotateAction.setEnabled(false);
            setCursor(mouseCursor);
            graphicHasChanged();
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

        /**
         * Is called if the mouse is dragged.
         * If this method returns false, the circuit is moved instead.
         *
         * @param e the mouse event
         * @return false is drag is not handled by controller
         */
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
        List<VisualElement> list = getCircuit().getElementListAt(pos, includeText);
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
                VisualElement vp = getVisualElement(pos, true);
                if (vp != null)
                    editAttributes(vp, e);
            } else if (mouse.isPrimaryClick(e) && hadFocusAtClick) {
                VisualElement vp = getVisualElement(pos, false);
                if (vp != null) {
                    if (vp.isPinPos(raster(pos)) && !mouse.isClickModifier(e)) {
                        if (!isLocked()) mouseWireRect.activate(pos);
                    } else
                        mouseMoveElement.activate(vp, pos);
                } else if (!isLocked()) {
                    if (mouse.isClickModifier(e)) {
                        Wire wire = getCircuit().getWireAt(pos, SIZE2);
                        if (wire != null)
                            mouseMoveWire.activate(wire, pos);
                    } else
                        mouseWireRect.activate(pos);
                }
            }
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
            element.setPos(pos.add(delta));
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
            if (mouse.isPrimaryClick(e) && !isLocked()) {
                modify(new ModifyInsertElement(element));
                insertWires(element);
            }
            mouseNormal.activate();
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

    private void insertWires(VisualElement element) {
        if (tutorialListener == null) {
            Modifications.Builder<Circuit> wires = new Modifications.Builder<>(Lang.get("lib_wires"));
            for (Pin p : element.getPins())
                insertWirePin(p, element, wires);
            modify(wires.build());
        }
    }

    private void insertWirePin(Pin p, VisualElement element, Modifications.Builder<Circuit> wires) {
        TransformRotate tr = new TransformRotate(new Vector(0, 0), element.getRotate());
        Vector pos = new Vector(-SIZE, 0);
        if (p.getDirection() != PinDescription.Direction.input)
            pos = new Vector(SIZE, 0);

        pos = tr.transform(pos);
        pos = pos.add(p.getPos());
        Pin found = null;
        List<VisualElement> el = getCircuit().getElementListAt(pos, false);
        for (VisualElement ve : el) {
            final Pin pinAt = ve.getPinAt(pos);
            if (pinAt != null) {
                if (found != null)
                    return;
                found = pinAt;
            }
            if (ve.isPinPos(p.getPos()))
                return;
        }
        if (found != null && PinDescription.Direction.isInOut(p.getDirection(), found.getDirection())) {
            Wire newWire = new Wire(found.getPos(), p.getPos());
            for (Wire w : getCircuit().getWires())
                if (w.equalsContent(newWire))
                    return;
            wires.add(new ModifyInsertWire(newWire));
        }
    }

    /**
     * Sets the circuit panel used for scrolling
     *
     * @param circuitScrollPanel the panel
     */
    public void setCircuitScrollPanel(CircuitScrollPanel circuitScrollPanel) {
        this.circuitScrollPanel = circuitScrollPanel;
        if (circuitScrollPanel != null)
            circuitScrollPanel.transformChanged(transform);
    }

    private final class MouseControllerMoveElement extends MouseController {
        private VisualElement visualElement;
        private Vector delta;
        private VisualElement originalVisualElement;

        private MouseControllerMoveElement(Cursor cursor) {
            super(cursor);
        }

        private void activate(VisualElement visualElement, Vector pos) {
            super.activate();
            originalVisualElement = visualElement;
            this.visualElement = new VisualElement(visualElement);
            shallowCopy = getCircuit().createShallowCopy();
            shallowCopy.delete(visualElement);
            delta = originalVisualElement.getPos().sub(pos);
            deleteAction.setEnabled(true);
            rotateAction.setEnabled(true);
            copyAction.setEnabled(true);
            graphicHasChanged();
        }

        @Override
        void clicked(MouseEvent e) {
            if (!isLocked()) {
                visualElement.setPos(visualElement.getPos());
                if (!visualElement.getPos().equals(originalVisualElement.getPos())
                        || visualElement.getRotate() != originalVisualElement.getRotate()) {
                    modify(new ModifyMoveAndRotElement(originalVisualElement, visualElement.getPos(), visualElement.getRotate()));
                    insertWires(visualElement);
                }
            }
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            if (!isLocked()) {
                Vector pos = getPosVector(e);
                visualElement.setPos(pos.add(delta));
                repaint();
            }
        }

        @Override
        public void drawTo(Graphic gr) {
            visualElement.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void delete() {
            if (!isLocked()) {
                getCircuit().delete(visualElement);
                isManualScale = true;
                modify(new ModifyDeleteElement(originalVisualElement));
                mouseNormal.activate();
            }
        }

        @Override
        public void rotate() {
            if (!isLocked()) {
                visualElement.rotate();
                repaint();
            }
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }

    private final class MouseControllerMoveWire extends MouseController {
        private Wire wire;
        private Vector pos;
        private Wire originalWire;

        private MouseControllerMoveWire(Cursor cursor) {
            super(cursor);
        }

        private void activate(Wire wire, Vector pos) {
            super.activate();
            originalWire = wire;
            shallowCopy = getCircuit().createShallowCopy();
            shallowCopy.delete(originalWire);
            this.wire = new Wire(wire);
            this.pos = raster(pos);
            deleteAction.setEnabled(true);
            removeHighLighted();
            graphicHasChanged();
        }

        @Override
        void clicked(MouseEvent e) {
            if (!originalWire.p1.equals(wire.p1)) {
                removeHighLighted();
                modify(new ModifyMoveWire(originalWire, wire));
                getCircuit().elementsMoved();
            }
            mouseNormal.activate();
        }

        @Override
        void moved(MouseEvent e) {
            Vector pos = raster(getPosVector(e));
            final Vector delta = pos.sub(this.pos);
            if (!delta.isZero()) {
                wire.move(delta);
                wire.noDot();
                isManualScale = true;
                repaint();
            }
            this.pos = pos;
        }

        @Override
        public void delete() {
            getCircuit().delete(wire);
            isManualScale = true;
            modify(new ModifyDeleteWire(originalWire));
            mouseNormal.activate();
        }

        @Override
        public void drawTo(Graphic gr) {
            wire.drawTo(gr, Style.HIGHLIGHT);
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
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
                Wire wire = getCircuit().getWireAt(pos, SIZE2);
                if (wire != null)
                    mouseMoveWire.activate(wire, pos);
            } else {
                if (mouse.isSecondaryClick(e))
                    mouseNormal.activate();
                else if (mouse.isPrimaryClick(e)) {
                    modify(new ModifyInsertWire(wire).checkIfLenZero());
                    if (getCircuit().isPinPos(wire.p2))
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
                Wire wire = getCircuit().getWireAt(pos, SIZE2);
                if (wire != null)
                    mouseMoveWire.activate(wire, pos);
            } else {
                if (mouse.isSecondaryClick(e))
                    mouseNormal.activate();
                else if (mouse.isPrimaryClick(e)) {
                    modify(new Modifications.Builder<Circuit>(Lang.get("mod_insertWire"))
                            .add(new ModifyInsertWire(wire1).checkIfLenZero())
                            .add(new ModifyInsertWire(wire2).checkIfLenZero())
                            .build());
                    if (getCircuit().isPinPos(wire2.p2))
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
        private Wire origWire;

        private MouseControllerWireSplit(Cursor cursor) {
            super(cursor);
        }

        private void activate(Wire w, Vector startPos) {
            super.activate();
            startPos = raster(startPos);
            origWire = w;
            shallowCopy = getCircuit().createShallowCopy();
            shallowCopy.delete(w);
            wire1 = new Wire(w.p1, startPos);
            wire2 = new Wire(startPos, w.p2);
        }

        @Override
        void moved(MouseEvent e) {
            Vector p = raster(getPosVector(e));
            wire1.setP2(p);
            wire2.setP1(p);
            repaint();
        }

        @Override
        void clicked(MouseEvent e) {
            if (mouse.isPrimaryClick(e)) {
                Modifications.Builder<Circuit> m = new Modifications.Builder<>(Lang.get("mod_splitWire"));
                m.add(new ModifyDeleteWire(origWire));
                m.add(new ModifyInsertWire(wire1));
                m.add(new ModifyInsertWire(wire2));
                modify(m.build());
                mouseNormal.activate();
            } else if (mouse.isSecondaryClick(e))
                escapePressed();
        }

        @Override
        public void escapePressed() {
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
            ArrayList<Drawable> elements = getCircuit().getElementsToHighlight(Vector.min(corner1, corner2), Vector.max(corner1, corner2));
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
                isManualScale = true;
                modify(new ModifyDeleteRect(Vector.min(corner1, corner2), Vector.max(corner1, corner2)));
                mouseNormal.activate();
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
        private Circuit.RectContainer elements;
        private Vector lastPos;
        private Vector center;
        private Vector accumulatedDelta;
        private int accumulatedRotate;
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

            accumulatedDelta = new Vector(0, 0);
            accumulatedRotate = 0;
            min = Vector.min(corner1, corner2);
            max = Vector.max(corner1, corner2);
            elements = getCircuit().copyElementsInRect(min, max, library.getShapeFactory());
            if (elements == null)
                mouseNormal.activate();
            else {
                shallowCopy = getCircuit().createShallowCopy();
                shallowCopy.delete(min, max);
            }
            removeHighLighted();
        }

        @Override
        void moved(MouseEvent e) {
            lastPos = getPosVector(e);
        }

        @Override
        boolean dragged(MouseEvent e) {
            Vector pos = getPosVector(e);
            Vector delta = raster(pos.sub(lastPos));

            if (delta.x != 0 || delta.y != 0) {
                for (Movable m : elements.getMovables())
                    m.move(delta);
                accumulatedDelta = accumulatedDelta.add(delta);

                repaint();
                lastPos = lastPos.add(delta);
                center = center.add(delta);
            }
            return true;
        }

        @Override
        void released(MouseEvent e) {
            if (accumulatedDelta.x != 0 || accumulatedDelta.y != 0 || accumulatedRotate != 0) {
                modify(new ModifyMoveSelected(min, max, accumulatedDelta, accumulatedRotate, center));
                getCircuit().elementsMoved();
            }
            mouseNormal.activate();
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            ModifyMoveSelected.rotateElements(elements.getMovables(), center);
            repaint();
            accumulatedRotate++;
        }

        @Override
        public void drawTo(Graphic gr) {
            for (Drawable m : elements.getDrawables())
                m.drawTo(gr, Style.HIGHLIGHT);
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
                Modifications.Builder<Circuit> builder = new Modifications.Builder<>(Lang.get("mod_insertCopied"));
                for (Movable m : elements) {
                    if (m instanceof Wire)
                        builder.add(new ModifyInsertWire((Wire) m));
                    if (m instanceof VisualElement)
                        builder.add(new ModifyInsertElement((VisualElement) m));
                }
                modify(builder.build());
            }
            mouseNormal.activate();
        }

        @Override
        public void rotate() {
            ModifyMoveSelected.rotateElements(elements, raster(lastPos));
            graphicHasChanged();
        }

        @Override
        public void escapePressed() {
            mouseNormal.activate();
        }
    }

    private interface Actor {
        void interact(CircuitComponent cc, Point p, Vector posInComponent, SyncAccess modelSync);
    }

    private final class MouseControllerRun extends MouseController {
        private VisualElement draggedElement;

        private MouseControllerRun(Cursor cursor) {
            super(cursor);
        }

        @Override
        void pressed(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null) {
                interact(e, (cc, pos, posInComponent, modelSync1) -> ve.elementPressed(cc, pos, posInComponent, modelSync1));
                draggedElement = ve;
            } else
                draggedElement = null;
        }

        private VisualElement getInteractiveElementAt(MouseEvent e) {
            List<VisualElement> elementList = getCircuit().getElementListAt(getPosVector(e), false);
            for (VisualElement ve : elementList) {
                if (ve.isInteractive())
                    return ve;
            }
            return null;
        }

        @Override
        void released(MouseEvent e) {
            if (draggedElement != null) {
                interact(e, (cc, pos, posInComponent, modelSync1) -> draggedElement.elementReleased(cc, pos, posInComponent, modelSync1));
                draggedElement = null;
            }
        }

        @Override
        void clicked(MouseEvent e) {
            VisualElement ve = getInteractiveElementAt(e);
            if (ve != null)
                interact(e, (cc, pos, posInComponent, modelSync1) -> ve.elementClicked(cc, pos, posInComponent, modelSync1));
        }

        @Override
        boolean dragged(MouseEvent e) {
            if (draggedElement != null) {
                interact(e, (cc, pos, posInComponent, modelSync1) -> draggedElement.elementDragged(cc, pos, posInComponent, modelSync1));
                return true;
            } else
                return false;
        }

        private void interact(MouseEvent e, Actor actor) {
            Point p = new Point(e.getX(), e.getY());
            SwingUtilities.convertPointToScreen(p, CircuitComponent.this);
            actor.interact(CircuitComponent.this, p, getPosVector(e), modelSync);
        }
    }

    /**
     * Activate a wizard
     *
     * @param wizardNotification the wizard notification
     */
    public void activateWizard(WizardNotification wizardNotification) {
        mouseNormal.activate();
        getCircuit().clearState();
        new MouseControllerWizard(wizardNotification).activate();
    }

    /**
     * Sets the modification listener.
     *
     * @param tutorialListener is called every time the circuit is modified
     */
    public void setTutorialListener(TutorialListener tutorialListener) {
        this.tutorialListener = tutorialListener;
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

    /**
     * Listener to get notified if the circuit has changed
     */
    public interface TutorialListener {
        /**
         * Called if the circuit was modified
         *
         * @param modification the modification
         */
        void modified(Modification<Circuit> modification);
    }
}
