/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * If added to a window, the focus is transferred to the given window.
 */
public class MoveFocusTo implements WindowFocusListener {
    private final Window parent;

    /**
     * Creates a new instance
     *
     * @param parent the window which should keep the focus
     */
    public MoveFocusTo(Window parent) {
        this.parent = parent;
    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        if (parent != null)
            SwingUtilities.invokeLater(parent::requestFocus);
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {

    }
}
