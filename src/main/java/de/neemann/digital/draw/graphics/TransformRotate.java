/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * Implements a rotation and translation.
 */
public class TransformRotate implements Transform {

    private final int sin;
    private final int cos;
    private final VectorInterface translation;

    /**
     * Creates a new instance
     *
     * @param translation the translation
     * @param rot         the rotation
     */
    public TransformRotate(VectorInterface translation, int rot) {
        this.translation = translation;
        switch (rot) {
            case 1:
                sin = 1;
                cos = 0;
                break;
            case 2:
                sin = 0;
                cos = -1;
                break;
            case 3:
                sin = -1;
                cos = 0;
                break;
            default:// 0
                sin = 0;
                cos = 1;
                break;
        }
    }

    @Override
    public Vector transform(Vector v) {
        return new Vector(v.getX() * cos + v.getY() * sin + translation.getX(),
                -v.getX() * sin + v.getY() * cos + translation.getY());
    }

    @Override
    public VectorFloat transform(VectorFloat v) {
        return new VectorFloat(v.getXFloat() * cos + v.getYFloat() * sin + translation.getXFloat(),
                -v.getXFloat() * sin + v.getYFloat() * cos + translation.getYFloat());
    }

    @Override
    public TransformMatrix getMatrix() {
        return new TransformMatrix(cos, sin, -sin, cos, translation.getXFloat(), translation.getYFloat());
    }
}
