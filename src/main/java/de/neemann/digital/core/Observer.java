/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * A simple observer
 */
public interface Observer {
    /**
     * is called if observable has changed
     */
    void hasChanged();
}
