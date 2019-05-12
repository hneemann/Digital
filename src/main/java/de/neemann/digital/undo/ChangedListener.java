/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

/**
 * Listener which is notified if the component has changed
 */
public interface ChangedListener {
    /**
     * Called if changed
     */
    void hasChanged();
}
