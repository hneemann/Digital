package de.neemann.digital.gui.draw.shapes;

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
     * shape can register the guiObserver to one of the the input or output ObservableValues.
     * To access the actual state while drawing, the Shape needs to store the IOState in a member
     * variable.
     *
     * If the shape returns an interactor, this interactors clicked method is called if the
     * shape is clicked in the running mode.
     *
     * @param ioState     the state of the part
     * @param guiObserver can be used to update the GUI by calling hasChanged, maybe null
     * @return the interactor is called if the shape is clicked during running mode, maybe null
     */
    Interactor applyStateMonitor(IOState ioState, Observer guiObserver);

}
