/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

/**
 * Interface used to stop the model.
 */
public interface ErrorStopper {
    /**
     * Called to stop the mode
     *
     * @param message the message
     * @param cause   the cause
     */
    void showErrorAndStopModel(String message, Exception cause);
}
