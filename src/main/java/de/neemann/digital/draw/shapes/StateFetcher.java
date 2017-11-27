package de.neemann.digital.draw.shapes;

/**
 * Allows fetching the state of a shape or wire.
 */
public interface StateFetcher {

    /**
     * Fetches the state of a shape by requesting data from the model.
     * During execution of this method the model is locked.
     * Thus this method should return as fast as possible.
     */
    default void fetch() {
    }

}
