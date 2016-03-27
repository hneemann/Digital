package de.neemann.digital.draw.shapes;

import de.neemann.digital.draw.graphics.Graphic;

/**
 * @author hneemann
 */
public interface Drawable {
    /**
     * Draws a element depending on its state
     *  @param graphic interface to draw to
     *
     */
    void drawTo(Graphic graphic);
}
