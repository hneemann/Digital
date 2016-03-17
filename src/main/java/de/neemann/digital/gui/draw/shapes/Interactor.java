package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public interface Interactor {
    void interact(CircuitComponent cc, Vector pos, State state);
}
