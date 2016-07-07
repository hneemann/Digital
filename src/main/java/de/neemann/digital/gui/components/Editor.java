package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

import javax.swing.*;

/**
 * @param <T> the type of the editor
 * @author hneemann
 */
public interface Editor<T> {

    /**
     * @return the value of the editor
     */
    T getValue();

    /**
     * Addes the components of the editor to the panel
     *
     * @param panel             the panel to add the components to
     * @param key               the key which is to edit
     * @param elementAttributes the attributes
     * @param dialog            the containing dialog
     */
    void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes, AttributeDialog dialog);
}
