/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm.gui;

import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.GraphicSwing;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.fsm.FSM;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 * The component to show the fsm
 */
public class FSMComponent extends JComponent {

    private boolean isManualScale;
    private AffineTransform transform = new AffineTransform();
    private FSM fsm;

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

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (!isManualScale)
                    fitFSM();
            }
        });

        setPreferredSize(new Dimension(600, 600));
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

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D gr2 = (Graphics2D) graphics;
        gr2.transform(transform);
        GraphicSwing gr = new GraphicSwing(gr2, 1);
        fsm.drawTo(gr);
    }
}
