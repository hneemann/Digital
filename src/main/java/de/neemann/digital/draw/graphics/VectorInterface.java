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

    /**
     * Creates a new vector which has the value this+a
     *
     * @param a a
     * @return this+a
     */
    VectorInterface add(VectorInterface a);

    /**
     * Creates a new vector which has the value this/d
     *
     * @param d d
     * @return this/d
     */
    VectorInterface div(int d);

    /**
     * Creates a new vector which has the value this*m
     *
     * @param m m
     * @return this*m
     */
    VectorFloat mul(float m);

    /**
     * Creates a new vector which has the value this-a
     *
     * @param a a
     * @return this-a
     */
    VectorInterface sub(VectorInterface a);

    /**
     * @return the norm of this vector
     */
    VectorFloat norm();

    /**
     * Rounds the vector to an int vector
     *
     * @return a int vector
     */
    Vector round();

    /**
     * @return returns a float vector
     */
    VectorFloat toFloat();

    /**
     * @return the length of the vector
     */
    float len();

    /**
     * @return a vector which is orthogonal to this one
     */
    VectorInterface getOrthogonal();

    /**
     * Calculates the scalar product
     *
     * @param v the vector to multiply with
     * @return the scalar product
     */
    default float scalar(VectorInterface v) {
        return getXFloat() * v.getXFloat() + getYFloat() * v.getYFloat();
    }

}
