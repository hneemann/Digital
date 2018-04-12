/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * The base interface of all vectors
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
     * Transforms the vector with the given transformation.
     *
     * @param tr the transformation to use
     * @return the transformed vector
     */
    VectorInterface transform(Transform tr);

}
