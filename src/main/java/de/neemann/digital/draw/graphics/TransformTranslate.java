/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A translation
 */
public class TransformTranslate implements Transform {
    private final VectorInterface trans;

    /**
     * Creates a new instance
     *
     * @param trans the translation
     */
    public TransformTranslate(VectorInterface trans) {
        this.trans = trans;
    }

    /**
     * Creates a new instance
     *
     * @param x the x translation
     * @param y the y translation
     */
    public TransformTranslate(int x, int y) {
        this(new Vector(x, y));
    }

    /**
     * Creates a new instance
     *
     * @param x the x translation
     * @param y the y translation
     */
    public TransformTranslate(float x, float y) {
        this(new VectorFloat(x, y));
    }

    @Override
    public Vector transform(Vector v) {
        return v.add(trans.getX(), trans.getY());
    }

    @Override
    public VectorFloat transform(VectorFloat v) {
        return new VectorFloat(v.getXFloat() + trans.getXFloat(), v.getYFloat() + trans.getYFloat());
    }

    @Override
    public TransformMatrix getMatrix() {
        return new TransformMatrix(1, 0, 0, 1, trans.getXFloat(), trans.getYFloat());
    }

    @Override
    public Transform invert() {
        return new TransformTranslate(trans.div(-1));
    }
}
