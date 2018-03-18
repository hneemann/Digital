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

    @Override
    public int getX() {
        return (int) x;
    }

    @Override
    public int getY() {
        return (int) y;
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
    public VectorFloat getVectorFloat() {
        return this;
    }

    /**
     * Subtracts the given vector
     *
     * @param sub the vector zo subtract
     * @return the new vector
     */
    public VectorFloat sub(VectorInterface sub) {
        return new VectorFloat(x - sub.getXFloat(), y - sub.getYFloat());
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
}
