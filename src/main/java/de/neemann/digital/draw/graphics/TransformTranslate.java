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

    @Override
    public Vector transform(Vector v) {
        return v.add(trans.getX(), trans.getY());
    }

    @Override
    public VectorFloat transform(VectorFloat v) {
        return new VectorFloat(v.getXFloat() + trans.getXFloat(), v.getYFloat() + trans.getYFloat());
    }
}
