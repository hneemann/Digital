/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

/**
 * Interface to acess the status line
 */
public interface StatusInterface {
    /**
     * Set message to the status line.
     * Is maybe calld from an non Swing thread. Therefore needs to be
     * thread save.
     *
     * @param message the message
     */
    void setStatus(String message);
}
