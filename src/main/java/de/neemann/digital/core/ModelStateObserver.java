package de.neemann.digital.core;

/**
 * Interface to implement observers of the model.
 *
 * @author hneemann
 */
public interface ModelStateObserver {

    /**
     * called if a event was detected.
     *
     * @param event the event
     */
    void handleEvent(ModelEvent event);

}
