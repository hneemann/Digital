/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * ModelStateObserver which can give information about the events it needs to be called
 */
public interface ModelStateObserverTyped extends ModelStateObserver {

    /**
     * @return the events on which this handler needs to be called
     */
    ModelEventType[] getEvents();

}
