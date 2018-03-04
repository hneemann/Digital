/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.remote;

/**
 * Exception thrown by the RemoteInterface
 */
public class RemoteException extends Exception {
    /**
     * Create a new Exception
     *
     * @param message the message
     */
    public RemoteException(String message) {
        super(message);
    }
}
