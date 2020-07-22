/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Interface to implement observers of the model.
 */
public interface ModelStateObserver {

    /**
     * called to propagate an event
     *
     * @param event the event
     */
    void handleEvent(ModelEvent event);

}
