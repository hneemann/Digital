package de.neemann.digital.core.element;

import de.neemann.digital.core.ObservableValues;

/**
 * @author hneemann
 */
public class PinDescriptions extends UnmutableList<PinDescription> {
    /**
     * Creates a new Instance
     *
     * @param items the items to store
     */
    public PinDescriptions(PinDescription... items) {
        super(items);
    }

    /**
     * Creates a new Instance
     *
     * @param observableValues the items to store
     */
    public PinDescriptions(ObservableValues observableValues) {
        super(observableValues);
    }
}
