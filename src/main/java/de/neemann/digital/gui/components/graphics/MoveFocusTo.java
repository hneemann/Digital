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
 * <p>
 * Should be attached to dialogs which offer no user interaction at all.
 * In other words, all windows that exclusively display something.
 * As soon as there are menus, buttons or similar, this listener should
 * no longer be used.
 */
public final class MoveFocusTo implements WindowFocusListener {

    /**
     * Adds the listener to the given child dialog and moves the focus to the given parent
     *
     * @param child  the child dialog
     * @param parent the parent window
     */
    public static void addListener(Dialog child, Window parent) {
        child.setAlwaysOnTop(true);
        child.addWindowFocusListener(new MoveFocusTo(child, parent));
    }

    private final Window child;
    private final Window parent;

    private MoveFocusTo(Window child, Window parent) {
        this.child = child;
        this.parent = parent;
    }

    @Override
    public void windowGainedFocus(WindowEvent windowEvent) {
        if (parent != null)
            SwingUtilities.invokeLater(() -> {
                child.removeWindowFocusListener(this);
                parent.requestFocus();
            });
    }

    @Override
    public void windowLostFocus(WindowEvent windowEvent) {

    }
}
