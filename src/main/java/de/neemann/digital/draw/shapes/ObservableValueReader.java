package de.neemann.digital.draw.shapes;

/**
 * Allows fetching the state of a shape or wire.
 */
public interface ObservableValueReader {

    /**
     * Fetches the state of a shape by requesting data from the model.
     * During execution of this method the model is locked.
     * Thus this method should return as fast as possible.
     *
     * The draw methods must not access the model data. This can lead to
     * unreasonable drawings.
     */
    default void readObservableValues() {
    }

}
