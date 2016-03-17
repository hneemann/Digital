package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.parts.Pins;

/**
 * @author hneemann
 */
public interface Shape extends Drawable {

    /**
     * Puts the pins name and the pins x-y-position together!
     *
     * @param partDescription
     * @return the pins
     */
    Pins getPins(PartDescription partDescription);

}
