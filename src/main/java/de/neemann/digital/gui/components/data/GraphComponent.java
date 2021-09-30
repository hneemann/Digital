/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.data;

import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.data.DataPlotter;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.graphics.GraphicSwing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The component to show the trace window.
 * It shows the data in the given dataSet.
 */
public class GraphComponent extends JComponent {
    private final DataPlotter plotter;

    /**
     * Creates a new dataSet
     *
     * @param dataSet   the dataSet to paint
     * @param modelSync lock to access the model
     */
    GraphComponent(ValueTable dataSet, SyncAccess modelSync) {
        plotter = new DataPlotter(dataSet, modelSync);
        addMouseWheelListener(e -> {
            double f = Math.pow(0.9, e.getWheelRotation());
            scale(f, e.getX());
        });

        addMouseMotionListener(new MouseAdapter() {
            private int lastxPos;
            private int lastyPos;

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                lastxPos = mouseEvent.getX();
                lastyPos = mouseEvent.getY();
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                int xPos = mouseEvent.getX();
                int yPos = mouseEvent.getY();
                plotter.move(xPos - lastxPos, yPos - lastyPos);
                lastxPos = xPos;
                lastyPos = yPos;
                repaint();
            }

        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                plotter.setWidth(getWidth());
                plotter.setHeight(getHeight());
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        plotter.drawTo(new GraphicSwing(g2), null);
    }

    @Override
    public Dimension getPreferredSize() {
        int w = plotter.getCurrentGraphicWidth();
        if (w < 600) w = 600;
        return new Dimension(w, plotter.getGraphicHeight());
    }

    /**
     * Apply a scaling factor
     *
     * @param f    the factor
     * @param xPos fixed position
     */
    public void scale(double f, int xPos) {
        plotter.scale(f, xPos);
        repaint();
    }

    /**
     * Fits the data to the visible area
     */
    void fitData() {
        plotter.fitInside();
        repaint();
    }

    /**
     * @return the data plotter
     */
    DataPlotter getPlotter() {
        return plotter;
    }

    /**
     * Sets the scroll bar to use
     *
     * @param horizontalScrollBar the scroll bar
     */
    void setHorizontalScrollBar(JScrollBar horizontalScrollBar) {
        plotter.setHorizontalScrollBar(horizontalScrollBar);
        horizontalScrollBar.addAdjustmentListener(adjustmentEvent -> {
            if (plotter.setNewXOffset(adjustmentEvent.getValue()))
                repaint();
        });
    }

    void setVerticalScrollBar(JScrollBar verticalScrollBar) {
        plotter.setVerticalScrollBar(verticalScrollBar);
        verticalScrollBar.addAdjustmentListener(adjustmentEvent -> {
            if (plotter.setNewYOffset(adjustmentEvent.getValue()))
                repaint();
        });
    }
}
