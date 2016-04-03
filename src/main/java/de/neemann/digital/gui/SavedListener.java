package de.neemann.digital.gui;

import java.io.File;

/**
 * @author hneemann
 */
public interface SavedListener {
    /**
     * Method is called if file is changed
     *
     * @param filename the changed file
     */
    void saved(File filename);
}
