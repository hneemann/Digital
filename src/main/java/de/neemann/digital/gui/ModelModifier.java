/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;

/**
 * Modifier which can be used to modify the model while it is started.
 * <p>
 * Its used by the {@link de.neemann.digital.gui.remote.RemoteSever}. The remote server passes an instance to
 * the start method ({@link Main#createAndStartModel(boolean, ModelEvent, ModelModifier)}) to modify the model
 * after its generation. It modifies the ROM node by copying the program to execute to the program memory.
 * <p>
 */
public interface ModelModifier {
    /**
     * Called before model.init() is called
     *
     * @param model the model
     * @throws NodeException NodeException
     */
    void preInit(Model model) throws NodeException;
}
