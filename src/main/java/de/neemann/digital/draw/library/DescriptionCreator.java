package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;

import java.io.IOException;

/**
 * Used for lazy loading of the circuits
 * Created by hneemann on 25.03.17.
 */
public interface DescriptionCreator {
    /**
     * Is called if the description is needed in the circuit.
     * Is not called to create the menus
     *
     * @return the description
     * @throws IOException IOException
     */
    ElementTypeDescription createDescription() throws IOException;
}
