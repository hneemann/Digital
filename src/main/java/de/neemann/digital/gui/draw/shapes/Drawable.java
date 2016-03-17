package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public interface Drawable {
    /**
     * Draws a part depending on its state
     *
     * @param graphic
     * @param state   maybe null
     */
    void drawTo(Graphic graphic, State state);
}
