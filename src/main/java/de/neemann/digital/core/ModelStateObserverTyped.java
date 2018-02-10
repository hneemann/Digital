package de.neemann.digital.core;

/**
 * ModelStateObserver which can give information about the events it needs to be called
 */
public interface ModelStateObserverTyped extends ModelStateObserver {

    /**
     * @return the events on which this handler needs to be called
     */
    ModelEvent[] getEvents();

}
