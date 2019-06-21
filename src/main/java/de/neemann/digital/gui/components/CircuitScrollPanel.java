/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.draw.graphics.GraphicMinMax;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A scroll panel used by the circuit component
 */
public class CircuitScrollPanel extends JPanel {
    private static final int BORDER = SIZE * 10;
    private final CircuitComponent circuitComponent;
    private final JScrollBar horizontal;
    private final JScrollBar vertical;
    private GraphicMinMax graphicMinMax;
    private AffineTransform transform;

    /**
     * Creates a new instance
     *
     * @param circuitComponent the circuit component to use
     */
    public CircuitScrollPanel(CircuitComponent circuitComponent) {
        super(new BorderLayout());
        horizontal = new JScrollBar(JScrollBar.HORIZONTAL);
        vertical = new JScrollBar(JScrollBar.VERTICAL);

        this.circuitComponent = circuitComponent;
        add(circuitComponent, BorderLayout.CENTER);
        add(horizontal, BorderLayout.SOUTH);
        add(vertical, BorderLayout.EAST);

        horizontal.addAdjustmentListener(adjustmentEvent -> {
            if (adjustmentEvent.getValueIsAdjusting())
                circuitComponent.translateCircuitToX(-adjustmentEvent.getValue() * transform.getScaleX());
        });
        vertical.addAdjustmentListener(adjustmentEvent -> {
            if (adjustmentEvent.getValueIsAdjusting())
                circuitComponent.translateCircuitToY(-adjustmentEvent.getValue() * transform.getScaleY());
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                if (transform != null)
                    updateBars();
            }
        });

        circuitComponent.setCircuitScrollPanel(this);
    }

    private GraphicMinMax getCircuitSize() {
        if (graphicMinMax == null) {
            graphicMinMax = new GraphicMinMax();
            circuitComponent.getCircuit().drawTo(graphicMinMax);
        }
        return graphicMinMax;
    }

    void sizeChanged() {
        graphicMinMax = null;
        if (transform != null)
            updateBars();
    }

    /**
     * Updates the transformation
     *
     * @param transform the transform
     */
    void transformChanged(AffineTransform transform) {
        this.transform = transform;
        updateBars();
    }

    private void updateBars() {
        GraphicMinMax gr = getCircuitSize();

        if (gr.getMin() == null || gr.getMax() == null || !circuitComponent.isManualScale()) {
            horizontal.setVisible(false);
            vertical.setVisible(false);
        } else {
            Point2D min = new Point2D.Float();
            Point2D max0 = new Point2D.Float();
            try {
                transform.inverseTransform(new Point2D.Float(0, 0), min);
                transform.inverseTransform(new Point2D.Float(getWidth(), getHeight()), max0);
                int viewDx = (int) (max0.getX() - min.getX());
                int viewDy = (int) (max0.getY() - min.getY());
                int valueX = (int) min.getX();
                int valueY = (int) min.getY();
                horizontal.setValues(valueX, viewDx, gr.getMin().x - BORDER, gr.getMax().x + BORDER);
                vertical.setValues(valueY, viewDy, gr.getMin().y - BORDER, gr.getMax().y + BORDER);


                checkVisibility(horizontal, viewDx, gr.getMax().x - gr.getMin().x + 2 * BORDER);
                checkVisibility(vertical, viewDy, gr.getMax().y - gr.getMin().y + 2 * BORDER);
            } catch (NoninvertibleTransformException e) {
                // can not happen! Scaling is never zero!
                e.printStackTrace();
            }
        }
    }

    private void checkVisibility(JScrollBar bar, int view, int size) {
        bar.setVisible(view < size);
    }

}
