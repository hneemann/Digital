/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Simple elper to detect a model error.
 */
public class ErrorDetector implements ModelStateObserverTyped {
    private Exception exception;

    @Override
    public ModelEventType[] getEvents() {
        return new ModelEventType[]{ModelEventType.ERROR_OCCURRED};
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event.getType() == ModelEventType.ERROR_OCCURRED)
            exception = event.getCause();
    }

    /**
     * Checks the error.
     * If there was an exception this exception is thrown
     *
     * @throws Exception Exception
     */
    public void check() throws Exception {
        if (exception != null)
            throw exception;
    }
}
