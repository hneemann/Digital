package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.graphics.Graphic;

/**
 * Interface implemented by the elements which can draw itself to a {@link Graphic} instance.
 *
 * @author hneemann
 */
public interface Drawable {
    /**
     * Draws an element depending on its state.
     *
     * @param graphic   interface to draw to
     * @param highLight true if a highlighted drawing is required
     */
    void drawTo(Graphic graphic, boolean highLight);
}
