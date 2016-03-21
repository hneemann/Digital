package de.neemann.digital.core;

/**
 * @author hneemann
 */
public interface ModelStateObserver {

    enum Event {STARTED, STOPPED}

    void handleEvent(Event event);

}
