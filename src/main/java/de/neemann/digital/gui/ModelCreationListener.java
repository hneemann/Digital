/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;

/**
 * Listener notified every time a model is created
 */
public interface ModelCreationListener {
    /**
     * Called if a model is created
     *
     * @param model the model created
     */
    void created(Model model);
}
