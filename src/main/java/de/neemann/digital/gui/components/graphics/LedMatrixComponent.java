/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import javax.swing.*;
import java.awt.*;

/**
 * Component to visualize a LED matrix
 */
public class LedMatrixComponent extends JComponent {
    private final int width;
    private final int height;
    private final long[] data;
    private final Color color;
    private final boolean ledPersist;
    private int lastCol;

    /**
     * Create a new instance
     *
     * @param dy         height of matrix
     * @param data       data
     * @param color      the LEDs color
     * @param ledPersist if true the LEDs light up indefinite
     */
    public LedMatrixComponent(int dy, long[] data, Color color, boolean ledPersist) {
        this.width = data.length;
        this.height = dy;
        this.data = data;
        this.color = color;
        this.ledPersist = ledPersist;

        int pw = 320 / width;
        if (pw < 2) pw = 2;
        int ph = 200 / height;
        if (ph < 2) ph = 2;
        int ledSize = (pw + ph) / 2;

        Dimension size = new Dimension(width * ledSize, height * ledSize);
        setPreferredSize(size);
        setOpaque(false);
    }

    /**
     * Update the graphic
     *
     * @param colAddr col update
     * @param rowData updated data
     */
    public void updateGraphic(int colAddr, long rowData) {
        lastCol = colAddr;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        for (int x = 0; x < width; x++) {
            int xPos = x * getWidth() / width;
            int dx = (x + 1) * getWidth() / width - xPos;
            long word = data[x];
            long mask = 1;
            for (int y = 0; y < height; y++) {

                boolean ledState = (word & mask) != 0;

                if (ledState && (ledPersist || (x == lastCol)))
                    g.setColor(color);
                else
                    g.setColor(Color.BLACK);

                int ypos = y * getHeight() / height;
                int dy = (y + 1) * getHeight() / height - ypos;

                g.fillOval(xPos, ypos, dx, dy);
                mask *= 2;
            }
        }
    }

}
