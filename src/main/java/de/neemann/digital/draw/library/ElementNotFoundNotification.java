package de.neemann.digital.draw.library;

import de.neemann.digital.core.element.ElementTypeDescription;

import java.io.File;

/**
 * Interface to request unknown {@link ElementTypeDescription}s
 *
 * @author hneemann
 */
public interface ElementNotFoundNotification {
    /**
     * Called if the {@link ElementLibrary} could not create an element
     *
     * @param file the elements file name
     * @return the element or null if not loadable
     */
    ElementTypeDescription elementNotFound(File file);
}
