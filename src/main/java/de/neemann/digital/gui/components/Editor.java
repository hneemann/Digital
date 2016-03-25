package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;

import java.awt.*;

/**
 * @author hneemann
 */
public interface Editor<T> {

    Component getComponent(ElementAttributes elementAttributes);

    T getValue();
}
