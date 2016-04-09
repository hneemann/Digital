package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.graphics.Graphic;

/**
 * Interface implemented by the elements which can draw itself at a {@link Graphic} instance.
 *
 * @author hneemann
 */
public interface Drawable {
    /**
     * Draws a element depending on its state
     *
     * @param graphic interface to draw to
     */
    void drawTo(Graphic graphic, boolean highLight);
}
