package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.parts.IOState;

/**
 * @author hneemann
 */
public interface Drawable {
    /**
     * Draws a part depending on its state
     *
     * @param graphic interface to draw to
     * @param ioState   maybe null, if not in running mode
     */
    void drawTo(Graphic graphic, IOState ioState);
}
