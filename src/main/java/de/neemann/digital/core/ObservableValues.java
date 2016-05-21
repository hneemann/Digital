package de.neemann.digital.core;

import de.neemann.digital.core.element.ImmutableList;

import java.util.Collection;

/**
 * @author hneemann
 */
public class ObservableValues extends ImmutableList<ObservableValue> {
    /**
     * An empty list
     */
    public static final ObservableValues EMPTY_LIST = new ObservableValues();

    /**
     * Helper to create a {@link ObservableValues} instance
     *
     * @param items the items
     * @return the created instance
     */
    public static ObservableValues ovs(ObservableValue... items) {
        return new ObservableValues(items);
    }

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
    public ObservableValues(Collection<ObservableValue> items) {
        super(items.toArray(new ObservableValue[items.size()]));
    }


    /**
     * Creates a new Instance
     *
     * @param items the items to store
     */
    public ObservableValues(ObservableValues items, int from, int to) {
        super(items, from, to);
    }

}
