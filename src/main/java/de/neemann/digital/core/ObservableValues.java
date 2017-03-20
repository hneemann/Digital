package de.neemann.digital.core;

import de.neemann.digital.core.element.ImmutableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author hneemann
 */
public class ObservableValues extends ImmutableList<ObservableValue> {

    /**
     * Builder for ObservableValues
     */
    public static class Builder {
        private ArrayList<ObservableValue> values;

        /**
         * create a builder
         */
        public Builder() {
            values = new ArrayList<>();
        }

        /**
         * Add values
         *
         * @param val values to add
         * @return the builder
         */
        public Builder add(ObservableValue... val) {
            values.addAll(Arrays.asList(val));
            return this;
        }

        /**
         * Add values
         *
         * @param val values to add
         * @return the builder
         */
        public Builder add(Collection<? extends ObservableValue> val) {
            values.addAll(val);
            return this;
        }

        /**
         * @return the {@link ObservableValues} instance
         */
        public ObservableValues build() {
            return new ObservableValues(values);
        }
    }


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
