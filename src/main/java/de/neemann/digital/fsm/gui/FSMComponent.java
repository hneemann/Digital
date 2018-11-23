/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.fsm.FSM;
import de.neemann.digital.fsm.Movable;
import de.neemann.digital.fsm.State;
import de.neemann.digital.fsm.Transition;
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
    private static final Key<String> KEY_VALUES = new Key<>("stateValues", "");
    private static final String DEL_ACTION = "myDelAction";

    private Mouse mouse = Mouse.getMouse();

    private boolean isManualScale;
    private AffineTransform transform = new AffineTransform();
    private Movable elementMoved;
    private FSM fsm;
    private Vector lastMousePos;

    /**
     * Creates a new component
     *
     * @param fsm the fsm to visualize
     */
    public FSMComponent(FSM fsm) {
        this.fsm = fsm;

        fsm.circle();

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
            private Vector delta;
            private Vector pos;

            @Override
            public void mousePressed(MouseEvent e) {
                pos = new Vector(e.getX(), e.getY());
                if (mouse.isPrimaryClick(e)) {
                    final Vector posVector = getPosVector(e);
                    elementMoved = fsm.getMovable(posVector);
                    if (elementMoved != null)
                        delta = posVector.sub(elementMoved.getPos());
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (elementMoved instanceof State)
                    ((State) elementMoved).toRaster();
                elementMoved = null;
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                if (mouse.isSecondaryClick(mouseEvent)) {
                    final Vector posVector = getPosVector(mouseEvent);
                    Movable elementClicked = fsm.getMovable(posVector);
                    if (elementClicked == null)
                        createNewState(posVector, new Point(mouseEvent.getX(), mouseEvent.getY()));
                    else if (elementClicked instanceof State)
                        editState((State) elementClicked, new Point(mouseEvent.getX(), mouseEvent.getY()));
                }
            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                lastMousePos = getPosVector(mouseEvent);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (elementMoved == null) {
                    Vector newPos = new Vector(e.getX(), e.getY());
                    Vector delta = newPos.sub(pos);
                    double s = transform.getScaleX();
                    transform.translate(delta.x / s, delta.y / s);
                    pos = newPos;
                    isManualScale = true;
                    repaint();
                } else {
                    elementMoved.setPos(getPosVector(e).sub(delta).toFloat());
                }
            }
        };
        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);

        ToolTipAction deleteAction = new ToolTipAction(Lang.get("menu_delete"), ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Movable element = fsm.getMovable(lastMousePos);
                if (element instanceof State)
                    fsm.remove((State) element);
                else if (element instanceof Transition)
                    fsm.remove((Transition) element);
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

    private void createNewState(Vector posVector, Point point) {
        ElementAttributes attr = new ElementAttributes();
        SwingUtilities.convertPointToScreen(point, this);
        AttributeDialog ad = new AttributeDialog(SwingUtilities.getWindowAncestor(this), point, attr, Keys.LABEL, KEY_NUMBER, KEY_VALUES);
        ElementAttributes newAttr = ad.showDialog();
        if (newAttr != null) {
            State s = new State(newAttr.get(Keys.LABEL))
                    .setPosition(posVector.toFloat())
                    .setNumber(newAttr.get(KEY_NUMBER))
                    .setValues(newAttr.get(KEY_VALUES));
            fsm.add(s);
            repaint();
        }
    }

    private void editState(State state, Point point) {
        ElementAttributes attr = new ElementAttributes()
                .set(KEY_NUMBER, state.getNumber())
                .set(KEY_VALUES, state.getValues())
                .set(Keys.LABEL, state.getName());
        SwingUtilities.convertPointToScreen(point, this);
        AttributeDialog ad = new AttributeDialog(SwingUtilities.getWindowAncestor(this),
                point, attr, Keys.LABEL, KEY_NUMBER, KEY_VALUES);
        ElementAttributes newAttr = ad.showDialog();
        if (newAttr != null) {
            state.setNumber(newAttr.get(KEY_NUMBER));
            state.setValues(newAttr.get(KEY_VALUES));
            state.setName(newAttr.get(Keys.LABEL));
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
    public void fitFSM() {
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

    /**
     * @return the element picked by the mouse
     */
    public Movable getElementMoved() {
        return elementMoved;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D gr2 = (Graphics2D) graphics;
        gr2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gr2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        gr2.transform(transform);
        GraphicSwing gr = new GraphicSwing(gr2, 1);
        fsm.drawTo(gr);
    }
}
