/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import java.awt.geom.AffineTransform;

/**
 * Used to store a affine transform
 */
public class TransformHolder {
    private final double[] matrix = new double[6];

    TransformHolder() {
        this(new AffineTransform());
    }

    TransformHolder(AffineTransform transform) {
        transform.getMatrix(matrix);
    }

    /**
     * @return true if the transform is the identity transform
     */
    public boolean isIdentity() {
        return createAffineTransform().isIdentity();
    }

    AffineTransform createAffineTransform() {
        return new AffineTransform(matrix);
    }
}
