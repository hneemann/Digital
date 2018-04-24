/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Interface of all representations of SVG-Elements
 * @author felix
 */
public interface SVGFragment {

    /**
     * Get Drawable representations of the elements
     * @return Array of Drawable Objects
     */
    SVGDrawable[] getDrawables();

    /**
     * Says if a Fragment is representing a pin
     * @return pin
     */
    default boolean isPin() {
        return false;
    }

    /**
     * Gets the Pos-Vector
     * @return Position
     */
    VectorInterface getPos();

    /**
     * Moves a Fragment up
     * @param diff
     *            Vector to subtract
     */
    void move(Vector diff);
}
