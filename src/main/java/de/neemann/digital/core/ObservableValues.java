package de.neemann.digital.core;

import de.neemann.digital.core.element.UnmutableList;

/**
 * @author hneemann
 */
public class ObservableValues extends UnmutableList<ObservableValue> {
    /**
     * Creates a new Instance
     *
     * @param items the items to store
     */
    public ObservableValues(ObservableValue... items) {
        super(items);
    }

    /**
     * Creates a new Instance
     *
     * @param items the items to store
     */
    public ObservableValues(ObservableValues items, int from, int to) {
        super(items, from, to);
    }

    /**
     * Copys the given list
     *
     * @param items the original data
     * @param from  inclusive
     * @param to    exclusive
     * @return the new list
     */
    public ObservableValues copyOfRange(ObservableValues items, int from, int to) {
        return new ObservableValues(items, from, to);
    }
}
