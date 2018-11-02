/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A simple transformation to a given vector
 */
public interface Transform {

    /**
     * The identity transform
     */
    Transform IDENTITY = new Transform() {
        @Override
        public Vector transform(Vector v) {
            return v;
        }

        @Override
        public VectorFloat transform(VectorFloat v) {
            return v;
        }

        @Override
        public TransformMatrix getMatrix() {
            return new TransformMatrix(1, 0, 0, 1, 0, 0);
        }
    };

    /**
     * Combines the two given transformations to a common transformation
     *
     * @param t1 first transformation
     * @param t2 second transformation
     * @return the resulting transformation
     */
    static Transform mul(Transform t1, Transform t2) {
        TransformMatrix m1 = t1.getMatrix();
        TransformMatrix m2 = t2.getMatrix();
        return new TransformMatrix(
                m1.a * m2.a + m1.b * m2.c,
                m1.c * m2.a + m1.d * m2.c,
                m1.a * m2.b + m1.b * m2.d,
                m1.c * m2.b + m1.d * m2.d,
                m1.a * m2.x + m1.b * m2.y + m1.x,
                m1.c * m2.x + m1.d * m2.y + m1.y);
    }


    /**
     * Transforms an integer vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    Vector transform(Vector v);

    /**
     * Transforms an float vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    VectorFloat transform(VectorFloat v);

    /**
     * Returns a matrix representation of this transformation
     *
     * @return the transformed Transform
     */
    TransformMatrix getMatrix();
}
