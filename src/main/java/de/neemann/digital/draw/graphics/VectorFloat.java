/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.util.Objects;

/**
 * A vector with float coordinates
 */
public class VectorFloat implements VectorInterface {

    private final float x;
    private final float y;

    /**
     * Creates a new vector
     *
     * @param x the x coordinate
     * @param y the x coordinate
     */
    public VectorFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a new vector which is a copy of the given vector.
     *
     * @param v the vector to copy
     */
    public VectorFloat(VectorInterface v) {
        this.x = v.getXFloat();
        this.y = v.getYFloat();
    }

    @Override
    public int getX() {
        return Math.round(x);
    }

    @Override
    public int getY() {
        return Math.round(y);
    }

    @Override
    public float getXFloat() {
        return x;
    }

    @Override
    public float getYFloat() {
        return y;
    }

    @Override
    public VectorInterface transform(Transform tr) {
        return tr.transform(this);
    }

    /**
     * Creates a new vector which has the value this+a
     *
     * @param a a
     * @return this+a
     */
    public VectorFloat add(VectorInterface a) {
        return new VectorFloat(x + a.getXFloat(), y + a.getYFloat());
    }

    /**
     * Creates a new vector which has the value this+(dx,dy)
     *
     * @param dx dx
     * @param dy dy
     * @return this+(dx,dy)
     */
    public VectorFloat add(float dx, float dy) {
        return new VectorFloat(x + dx, y + dy);
    }

    /**
     * Subtracts the given vector
     *
     * @param sub the vector to subtract
     * @return the new vector
     */
    public VectorFloat sub(VectorInterface sub) {
        return new VectorFloat(x - sub.getXFloat(), y - sub.getYFloat());
    }

    @Override
    public VectorFloat norm() {
        float l = (float) Math.sqrt(x * x + y * y);
        return new VectorFloat(x / l, y / l);
    }

    /**
     * Creates a new vector which has the value this*a
     *
     * @param a a
     * @return this*a
     */
    public VectorFloat mul(float a) {
        return new VectorFloat(x * a, y * a);
    }

    /**
     * Returns the sclar product
     *
     * @param p the other vector
     * @return the scalar procuct
     */
    public float mul(VectorFloat p) {
        return x * p.x + y * p.y;
    }

    /**
     * Creates a new vector which has the value this/d
     *
     * @param d a
     * @return this/d
     */
    public VectorFloat div(int d) {
        return new VectorFloat(x / d, y / d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VectorFloat that = (VectorFloat) o;
        return Float.compare(that.x, x) == 0
                && Float.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(x=" + x
                + ", y=" + y
                + ')';
    }

    @Override
    public Vector round() {
        return new Vector(getX(), getY());
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public VectorFloat toFloat() {
        return this;
    }

    @Override
    public VectorFloat getOrthogonal() {
        return new VectorFloat(y, -x);
    }

}
