package de.neemann.digital.gui.components;

import java.awt.*;

/**
 * @author hneemann
 */
public interface Editor<T> {

    Component getComponent();

    T getValue();
}
