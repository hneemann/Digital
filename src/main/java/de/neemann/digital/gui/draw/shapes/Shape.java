package de.neemann.digital.gui.draw.shapes;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Observer;
import de.neemann.digital.gui.draw.parts.IOState;
import de.neemann.digital.gui.draw.parts.Pins;

/**
 * @author hneemann
 */
public interface Shape extends Drawable {

    /**
     * Puts the pins name and the pins x-y-position together!
     * This information is used to calculate the models connections
     * from the wiring in the circuit.
     *
     * @return the pins
     */
    Pins getPins();

    /**
     * If the look of the shape depends on an input or output state, the
     * shape can register a listener to one of the the states ObservableValues.
     * If the listener decides to update the GUI it can call listener.needsUpdate.
     *
     * @param ioState       the state of the part
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     * @param model       the model
     * @return the interactor is called if the shape is clicked during running mode
     */
    Interactor applyStateMonitor(IOState ioState, Observer guiObserver, Model model);

}
