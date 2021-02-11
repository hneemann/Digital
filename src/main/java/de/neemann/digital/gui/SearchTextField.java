/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * JTextField which shows the text "search" if the field is empty.
 */
public class SearchTextField extends JTextField {

    /**
     * Creates a new instance
     */
    public SearchTextField() {
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            g.drawString(Lang.get("msg_search"), 5, (getHeight() + getFont().getSize()) / 2);
        }
    }
}
