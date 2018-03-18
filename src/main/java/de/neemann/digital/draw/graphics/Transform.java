/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A simple transformation to a given vector
 */
public abstract class Transform {

    /**
     * Transforms an integer vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    public abstract Vector transform(Vector v);

    /**
     * Transforms an float vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    public abstract VectorFloat transform(VectorFloat v);

    /**
     * Transforms an vector interface
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    public VectorInterface transform(VectorInterface v) {
        if (v instanceof Vector)
            return transform((Vector) v);
        else
            return transform(v.getVectorFloat());
    }

}
