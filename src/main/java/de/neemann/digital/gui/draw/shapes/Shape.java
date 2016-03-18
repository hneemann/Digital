package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.gui.draw.parts.Pins;
import de.neemann.digital.gui.draw.parts.State;

/**
 * @author hneemann
 */
public interface Shape extends Drawable {

    /**
     * Puts the pins name and the pins x-y-position together!
     *
     * @return the pins
     */
    Pins getPins();

    /**
     * If the look of the shape depends on an input or output state, the
     * shape can register a state monitor to the state.
     * If the monitor decides to update the GUI it can call listener.needsUpdate.
     * Caution: Don't store state in the shape itself!
     *
     * @param state the state
     */
    Interactor applyStateMonitor(State state, Listener listener, Model model);

}
