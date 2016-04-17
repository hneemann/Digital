package de.neemann.digital.gui.state;

/**
 * A simple state
 *
 * @author hneemann
 */
public interface StateInterface {
    /**
     * Is called if the state is entered
     */
    void enter();

    /**
     * Is called if the state is leaved
     */
    void leave();

}
