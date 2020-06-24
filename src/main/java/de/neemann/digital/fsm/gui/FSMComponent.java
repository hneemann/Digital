/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.*;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.fsm.*;
import de.neemann.digital.gui.components.AttributeDialog;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.Mouse;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static de.neemann.digital.gui.components.CircuitComponent.ICON_DELETE;

/**
 * The component to show the fsm
 */
public class FSMComponent extends JComponent {
    private static final Key<Integer> KEY_NUMBER = new Key.KeyInteger("stateNum", 0);
    private static final Key<Boolean> KEY_INITIAL = new Key<>("isInitialState", false);
    private static final Key<String> KEY_VALUES = new Key<>("stateValues", "");
    private static final Key<String> KEY_CONDITION = new Key<>("transCond", "");
    private static final Key<Integer> KEY_RADIUS = new Key.KeyInteger("transRad", 70)
            .setComboBoxValues(50, 70, 90);
    private static final String DEL_ACTION = "myDelAction";
    private static final int MIN_NEW_TRANS_DIST = 10;

    private final Mouse mouse = Mouse.getMouse();

    private boolean isManualScale;
    private AffineTransform transform = new AffineTransform();
    private MouseMovable elementMoved;
    private FSM fsm;
    private Vector lastMousePos;
    private State newTransitionFromState;
    private Vector newTransitionStartPos;
    private String lastCondition = "";

    /**
     * Creates a new component
     */
    FSMComponent() {
        addMouseWheelListener(e -> {
            Vector pos = getPosVector(e);
            double f = Math.pow(0.9, e.getWheelRotation());
            transform.translate(pos.x, pos.y);
            transform.scale(f, f);
            transform.translate(-pos.x, -pos.y);
            isManualScale = true;
            repaint();
        });

        MouseAdapter mouseListener = new MouseAdapter() {
            private boolean screenDrag;
            private Vector delta;
            private Vector pos;

            @Override
            public void mousePressed(MouseEvent e) {
                pos = new Vector(e.getX(), e.getY());
                final Vector posVector = getPosVector(e);
                screenDrag = false;
                if (mouse.isPrimaryClick(e)) {
                    elementMoved = fsm.getMovable(posVector);
                    if (elementMoved != null)
                        delta = posVector.sub(elementMoved.getPos());
                } else if (mouse.isSecondaryClick(e)) {
                    MouseMovable st = fsm.getMovable(posVector);
                    if (st instanceof State) {
                        newTransitionStartPos = posVector;
                        newTransitionFromState = (State) st;
                        repaint();
                    }
                    screenDrag = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (elementMoved != null) {
                    elementMoved.setPos(getPosVector(mouseEvent).sub(delta).toFloat());
                    repaint();
                }
                elementMoved = null;
                if (newTransitionFromState != null) {
                    final Vector posVector = getPosVector(mouseEvent);
                    if (newTransitionStartPos.sub(posVector).len() > MIN_NEW_TRANS_DIST) {
                        MouseMovable target = fsm.getMovable(posVector);
                        if (target instanceof State)
                            fsm.add(new Transition(newTransitionFromState, (State) target, lastCondition));
                    }
                    newTransitionFromState = null;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                final Vector posVector = getPosVector(mouseEvent);
                MouseMovable elementClicked = fsm.getMovable(posVector);
                if (mouse.isSecondaryClick(mouseEvent)) {
                    if (elementClicked == null)
                        createNewState(posVector, new Point(mouseEvent.getX(), mouseEvent.getY()));
                    else if (elementClicked instanceof State)
                        editState((State) elementClicked, new Point(mouseEvent.getX(), mouseEvent.getY()));
                    else if (elementClicked instanceof Transition)
                        editTransition((Transition) elementClicked, new Point(mouseEvent.getX(), mouseEvent.getY()));
                }
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                lastMousePos = getPosVector(mouseEvent);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                lastMousePos = getPosVector(e);
                if (elementMoved == null && newTransitionFromState == null && screenDrag) {
                    Vector newPos = new Vector(e.getX(), e.getY());
                    Vector delta = newPos.sub(pos);
                    double s = transform.getScaleX();
                    transform.translate(delta.x / s, delta.y / s);
                    pos = newPos;
                    isManualScale = true;
                    repaint();
                }
                if (elementMoved != null) {
                    elementMoved.setPosDragging(getPosVector(e).sub(delta).toFloat());
                    repaint();
                }
                if (newTransitionFromState != null)
                    repaint();
            }
        };
        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);

        ToolTipAction deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                MouseMovable element = fsm.getMovable(lastMousePos);
                if (element instanceof State) {
                    fsm.remove((State) element);
                    repaint();
                } else if (element instanceof Transition) {
                    fsm.remove((Transition) element);
                    repaint();
                }
            }
        }.setToolTip(Lang.get("menu_delete_tt"));

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DEL_ACTION);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), DEL_ACTION);
        getActionMap().put(DEL_ACTION, deleteAction);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (!isManualScale)
                    fitFSM();
            }
        });

        setFocusable(true);
        setPreferredSize(new Dimension(600, 600));
    }

    private static final Key<?>[] STATE_EDIT_KEYS = {Keys.LABEL, KEY_NUMBER, KEY_INITIAL, KEY_VALUES, KEY_RADIUS};

    private void createNewState(Vector posVector, Point point) {
        ElementAttributes attr = new ElementAttributes();
        attr.set(KEY_NUMBER, fsm.getStates().size());
        SwingUtilities.convertPointToScreen(point, this);
        AttributeDialog ad = new AttributeDialog(SwingUtilities.getWindowAncestor(this),
                point, attr, STATE_EDIT_KEYS)
                .setDialogTitle(Lang.get("msg_fsmNewState"));
        ElementAttributes newAttr = ad.showDialog();

        if (newAttr == null && ad.isOkPressed())
            newAttr = attr;

        if (newAttr != null) {
            fsm.add(new State(newAttr.get(Keys.LABEL))
                    .setNumber(newAttr.get(KEY_NUMBER))
                    .setValues(newAttr.get(KEY_VALUES))
                    .setPosition(posVector.toFloat())
                    .toRaster());
            repaint();
        }
    }

    private void editState(State state, Point point) {
        ElementAttributes attr = new ElementAttributes()
                .set(KEY_NUMBER, state.getNumber())
                .set(KEY_INITIAL, state.isInitial())
                .set(KEY_VALUES, state.getValues())
                .set(KEY_RADIUS, state.getVisualRadius())
                .set(Keys.LABEL, state.getName());
        SwingUtilities.convertPointToScreen(point, this);
        ElementAttributes newAttr = new AttributeDialog(SwingUtilities.getWindowAncestor(this),
                point, attr, STATE_EDIT_KEYS)
                .setDialogTitle(Lang.get("msg_fsmState"))
                .showDialog();
        if (newAttr != null) {
            state.setNumber(newAttr.get(KEY_NUMBER));
            state.setInitial(newAttr.get(KEY_INITIAL));
            state.setValues(newAttr.get(KEY_VALUES));
            state.setRadius(newAttr.get(KEY_RADIUS));
            state.setName(newAttr.get(Keys.LABEL));
            repaint();
        }
    }

    private void editTransition(Transition transition, Point point) {
        ElementAttributes attr = new ElementAttributes()
                .set(KEY_CONDITION, transition.getCondition())
                .set(KEY_VALUES, transition.getValues());
        SwingUtilities.convertPointToScreen(point, this);
        ElementAttributes newAttr = new AttributeDialog(SwingUtilities.getWindowAncestor(this),
                point, attr, KEY_CONDITION, KEY_VALUES)
                .setDialogTitle(Lang.get("msg_fsmTransition"))
                .showDialog();
        if (newAttr != null) {
            lastCondition = newAttr.get(KEY_CONDITION);
            transition.setCondition(lastCondition);
            transition.setValues(newAttr.get(KEY_VALUES));
            repaint();
        }
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
     * Fits the FSM to the window
     */
    void fitFSM() {
        if (fsm != null) {
            GraphicMinMax gr = new GraphicMinMax();
            fsm.drawTo(gr);

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
                repaint();
            }
        }
    }

    /**
     * scales the fsm
     *
     * @param f factor to scale
     */
    void scaleCircuit(double f) {
        Vector dif = getPosVector(getWidth() / 2, getHeight() / 2);
        transform.translate(dif.x, dif.y);
        transform.scale(f, f);
        transform.translate(-dif.x, -dif.y);
        isManualScale = true;
        repaint();
    }

    /**
     * @return the element picked by the mouse
     */
    MouseMovable getElementMoved() {
        return elementMoved;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(ColorScheme.getSelected().getColor(ColorKey.BACKGROUND));
        graphics.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D gr2 = (Graphics2D) graphics;
        gr2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        gr2.transform(transform);
        GraphicSwing gr = new GraphicSwing(gr2, 1);
        fsm.drawTo(gr);

        if (newTransitionFromState != null) {
            final Vector dif = lastMousePos.sub(newTransitionStartPos);
            int max = Math.max(Math.abs(dif.x), Math.abs(dif.y));
            if (max > MIN_NEW_TRANS_DIST) {
                VectorFloat d = lastMousePos.sub(newTransitionFromState.getPos()).norm().mul(16f);
                VectorFloat a = d.getOrthogonal().norm().mul(8f);
                gr.drawPolygon(new Polygon(false)
                        .add(lastMousePos.sub(d).add(a))
                        .add(lastMousePos)
                        .add(lastMousePos.sub(d).sub(a)), Style.SHAPE_PIN);
                gr.drawLine(newTransitionFromState.getPos(), lastMousePos.sub(d.mul(0.2f)), Style.SHAPE_PIN);
            }
        }
    }

    /**
     * Sets the fsm to show
     *
     * @param fsm the fsm to show
     */
    public void setFSM(FSM fsm) {
        this.fsm = fsm;
        fitFSM();
        repaint();
    }
}
