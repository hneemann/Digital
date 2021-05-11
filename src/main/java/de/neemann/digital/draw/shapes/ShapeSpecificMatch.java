/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.graphics.Vector;

/**
 * A shape where the clickable area is not simply the bounding box, but
 * defined by the shape itself.
 */
public interface ShapeSpecificMatch extends Shape {

    /**
     * Checks is the given position matches the shape
     *
     * @param pos the position
     * @return true if the position matches the shape
     */
    boolean matches(Vector pos);
}
