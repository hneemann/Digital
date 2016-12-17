package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;

/**
 * Modifier which can be used to modify the model while it is started
 * Created by hneemann on 17.12.16.
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
