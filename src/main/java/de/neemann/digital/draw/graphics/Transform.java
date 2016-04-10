package de.neemann.digital.draw.graphics;

/**
 * A simple transformation to a given vector
 *
 * @author hneemann
 */
public interface Transform {
    /**
     * Transforms a vector
     *
     * @param v the vector to transform
     * @return the transformed vector
     */
    Vector transform(Vector v);
}
