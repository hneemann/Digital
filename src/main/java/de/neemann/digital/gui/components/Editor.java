package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;

import javax.swing.*;

/**
 * @author hneemann
 */
public interface Editor<T> {

    T getValue();

    void addToPanel(JPanel panel, AttributeKey key, ElementAttributes elementAttributes);
}
