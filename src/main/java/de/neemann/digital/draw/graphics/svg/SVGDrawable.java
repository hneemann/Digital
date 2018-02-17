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
