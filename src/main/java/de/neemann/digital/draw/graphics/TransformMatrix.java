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
     * Rotates in mathematically positive direction. Takes into account that
     * in Digital the y-axis goes downwards.
     *
     * @param w the angle in 360 grad units
     * @return the transformation
     */
    public static TransformMatrix rotate(double w) {
        final double phi = w / 180 * Math.PI;
        float cos = (float) Math.cos(phi);
        float sin = (float) Math.sin(phi);
        return new TransformMatrix(cos, -sin, sin, cos, 0, 0);
    }

    /**
     * Creates a scaling transformation
     *
     * @param sx scaling in x direction
     * @param sy scaling in y direction
     * @return the transformation
     */
    public static TransformMatrix scale(float sx, float sy) {
        return new TransformMatrix(sx, 0, 0, sy, 0, 0);
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
     * Transforms a direction vector.
     * Ignores the translation part of the transformation.
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

    /**
     * Returns the inverse transformation.
     *
     * @return the inverse transformation.
     */
    public TransformMatrix invert() {
        float q = a * d - b * c;

        return new TransformMatrix(d / q, -b / q, -c / q, a / q,
                (b * y - d * x) / q, (c * x - a * y) / q);
    }

    /**
     * @return true if this transformation does no asymmetric scaling.
     */
    public boolean isUniform() {
        return equal(a, d) && equal(b, -c);
    }

    /**
     * @return true if this transformation does no rotation
     */
    public boolean noRotation() {
        return equal(b, 0) && equal(c, 0);
    }

    private static boolean equal(float a, float b) {
        return Math.abs(a - b) < 1e-7;
    }

    /**
     * @return the matrix values
     */
    public float[] getValues() {
        return new float[]{a, b, c, d, x, y};
    }

}





















