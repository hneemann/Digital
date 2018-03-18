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

}
