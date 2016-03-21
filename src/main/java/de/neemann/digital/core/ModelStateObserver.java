package de.neemann.digital.core;

/**
 * @author hneemann
 */
public interface ModelStateObserver {

    void handleEvent(ModelEvent event);

}
