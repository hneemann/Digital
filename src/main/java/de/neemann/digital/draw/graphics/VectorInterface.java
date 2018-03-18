/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * The bas class of all vectors
 */
public interface VectorInterface {

    /**
     * @return the integer x coordinate
     */
    int getX();

    /**
     * @return the integer y coordinate
     */
    int getY();

    /**
     * @return the float x coordinate
     */
    float getXFloat();

    /**
     * @return the float y coordinate
     */
    float getYFloat();

    /**
     * @return this vactor as a {@link VectorFloat} instance
     */
    VectorFloat getVectorFloat();
}
