package de.neemann.digital.core;

/**
 * A simple observer
 *
 * @author hneemann
 */
public interface Observer {
    /**
     * is called if observable has changed
     */
    void hasChanged();
}
