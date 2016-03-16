package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.PartDescription;
import de.neemann.digital.gui.draw.parts.Pin;

/**
 * @author hneemann
 */
public interface Shape extends Drawable {

    Iterable<Pin> getPins(PartDescription partDescription);

}
