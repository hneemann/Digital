package de.neemann.digital.core;

import de.neemann.digital.draw.elements.PinException;

/**
 * The simplest possible node
 * Created by hneemann on 11.06.17.
 */
public interface NodeInterface extends Observer {

    /**
     * returns the outputs effected by this node
     *
     * @return the outputs
     * @throws PinException PinException
     */
    ObservableValues getOutputs() throws PinException;

}
