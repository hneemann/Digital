/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;

import javax.swing.*;
import java.awt.*;

/**
 * Simple Dialog to show all possible functions of a truth table
 */
public class AllSolutionsDialog extends JDialog {
    private final JTextPane textPane;
    private final JScrollPane scroll;

    /**
     * Creates a new Frame
     *
     * @param owner the owner frame
     * @param font  the font to use
     */
    public AllSolutionsDialog(JDialog owner, Font font) {
        super(owner, Lang.get("win_allSolutions"), false);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setFont(font);
        textPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setEditable(false);
        textPane.setPreferredSize(Screen.getInstance().scale(new Dimension(600, 300)));

        scroll = new JScrollPane(textPane);
        getContentPane().add(scroll);
        pack();
        setLocation(0, 0);
    }

    /**
     * Sets the given text to the frame
     *
     * @param text the text
     * @return this for call chaining
     */
    public AllSolutionsDialog setText(String text) {
        textPane.setText(text);
        SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new Point(0, 0)));
        return this;
    }
}
