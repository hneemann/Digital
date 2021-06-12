/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Class that calculates whether elements are visible on screen and should be drawn.
 */
public final class VisibilityCalculator {
    private VisibilityCalculator() {
        // Not called because this is a utility class.
    }

    /**
     * Calculate whether a line is visible on screen.
     *
     * @param p1 Point 1 of the line
     * @param p2 Point 2 of the line
     * @param gr Graphics on which the line would be drawn
     * @param rasterWidth Width of the screen buffer
     * @param rasterHeight Height of the screen buffer
     * @return Visible
     */
    public static boolean isLineVisible(VectorInterface p1, VectorInterface p2, Graphics2D gr,
                                        int rasterWidth, int rasterHeight) {
        AffineTransform transform = gr.getTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();
        double minX = -transform.getTranslateX() / scaleX;
        double maxX = minX + (rasterWidth / scaleX);
        double minY = -transform.getTranslateY() / scaleY;
        double maxY = minY + (rasterHeight / scaleY);

        double p1X = p1.getX();
        double p1Y = p1.getY();
        double p2X = p2.getX();
        double p2Y = p2.getY();

        boolean intersects = true;
        if (p1Y == p2Y) {
            // Horizontal line
            intersects = !(p1X < minX && p2X < minX || p1X > maxX && p2X > maxX) && p1Y >= minY && p1Y <= maxY;
        } else if (p1X == p2X) {
            // Vertical line
            intersects = !(p1Y < minY && p2Y < minY || p1Y > maxY && p2Y > maxY) && p1X >= minX && p1X <= maxX;
        }

        return intersects;
    }

}
