package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;

/**
 * Interface to implement the events used to be reverted
 * Created by hneemann on 25.05.17.
 */
public interface Modification {

    /**
     * Performs a modification on the given circuit
     *
     * @param circuit the circuit to modify
     */
    void modify(Circuit circuit);

}
