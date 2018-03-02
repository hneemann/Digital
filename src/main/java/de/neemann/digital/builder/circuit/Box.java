/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

/**
 * Width and height of a fragment
 */
public class Box {

    private final int width;
    private final int height;

    /**
     * Create a new instance
     *
     * @param width  width
     * @param height height
     */
    public Box(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return height
     */
    public int getHeight() {
        return height;
    }
}
