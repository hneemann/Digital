package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Interface for drawable primitives
 * @author felix
 */
public interface Drawable {

    /**
     * Draws the primitive
     * @param graphic
     *            to draw on
     * @param pos
     *            where to draw
     */
    void draw(Graphic graphic, Vector pos);

}
