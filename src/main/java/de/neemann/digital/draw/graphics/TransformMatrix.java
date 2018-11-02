/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A Matrix transformation
 */
public class TransformMatrix implements Transform {


    /**
     * Creates a rotation.
     *
     * @param w the angle in 360 grad
     * @return the transformation
     */
    public static TransformMatrix rotate(float w) {
        final double phi = w / 180 * Math.PI;
        float cos = (float) Math.cos(phi);
        float sin = (float) Math.sin(phi);
        return new TransformMatrix(cos, sin, -sin, cos, 0, 0);
    }

    final float a;
    final float b;
    final float c;
    final float d;
    final float x;
    final float y;

    /**
     * Creates a new instance
     *
     * @param a A_00
     * @param b A_10
     * @param c A_01
     * @param d A_11
     * @param x x offset
     * @param y y offset
     */
    public TransformMatrix(float a, float b, float c, float d, float x, float y) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.x = x;
        this.y = y;
    }

    @Override
    public Vector transform(Vector v) {
        return new Vector(
                (int) (v.getXFloat() * a + v.getYFloat() * b + x),
                (int) (v.getXFloat() * c + v.getYFloat() * d + y));
    }

    @Override
    public VectorFloat transform(VectorFloat v) {
        return new VectorFloat(
                v.getXFloat() * a + v.getYFloat() * b + x,
                v.getXFloat() * c + v.getYFloat() * d + y);
    }


    /**
     * Transforms a direction vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    public VectorFloat transformDirection(VectorInterface v) {
        return new VectorFloat(
                v.getXFloat() * a + v.getYFloat() * b,
                v.getXFloat() * c + v.getYFloat() * d);
    }

    @Override
    public TransformMatrix getMatrix() {
        return this;
    }

}
