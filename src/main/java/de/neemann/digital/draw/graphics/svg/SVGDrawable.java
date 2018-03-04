/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.draw.graphics.Graphic;

/**
 * Interface for drawable primitives
 * @author felix
 */
public interface SVGDrawable {

    /**
     * Draws the primitive
     * @param graphic
     *            to draw on
     */
    void draw(Graphic graphic);
}
