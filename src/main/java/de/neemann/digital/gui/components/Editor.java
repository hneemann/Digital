package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;

import javax.swing.*;

/**
 * @author hneemann
 */
public interface Editor<T> {

    T getValue();

    void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes);
}
