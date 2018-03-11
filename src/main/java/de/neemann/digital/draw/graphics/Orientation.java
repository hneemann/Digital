/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * The text orientation
 */
public enum Orientation {
    /**
     * the anchor point is at the left side of the text at the bottom line
     */
    LEFTBOTTOM(0, 0),
    /**
     * the anchor point is at the center of the text at the bottom line
     */
    CENTERBOTTOM(1, 0),
    /**
     * the anchor point is at the right side of the text at the bottom line
     */
    RIGHTBOTTOM(2, 0),
    /**
     * the anchor point is at the right side of the text in middle height
     */
    RIGHTCENTER(2, 1),
    /**
     * the anchor point is at the right side of the text at the top of the text
     */
    RIGHTTOP(2, 2),
    /**
     * the anchor point is at the center of the text at the top of the text
     */
    CENTERTOP(1, 2),
    /**
     * the anchor point is at the left side of the text at the top of the text
     */
    LEFTTOP(0, 2),
    /**
     * the anchor point is at the left side of the text in middle height
     */
    LEFTCENTER(0, 1),
    /**
     * the anchor point is in the center of the text
     */
    CENTERCENTER(1, 1);


    private final int x;
    private final int y;

    Orientation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x orientation value 0,1,2
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y orientation value 0,1,2
     */
    public int getY() {
        return y;
    }

    /**
     * ROTATE this orientation by the given angle.
     *
     * @param n the angle in units of 45 degrees
     * @return the new orientation
     */
    public Orientation rot(int n) {
        if (this == CENTERCENTER) return CENTERCENTER;

        int p = this.ordinal() + n * 2;
        if (p > 7) p = p - 8;
        if (p < 0) p = p + 8;
        return Orientation.values()[p];
    }

}
