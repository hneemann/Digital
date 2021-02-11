/*
 * Copyright (c) 2020 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */

package de.neemann.digital.gui.components.tree;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.lang.Lang;

/**
 * Search bar to filter components in tree.
 */
public class SelectSearch extends JTextField {

    private static final int SEARCH_DEBOUNCE_DELAY = 100;

    public SelectSearch(SelectTree tree, ElementLibrary library) {
        super();

        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        Timer timer = new Timer(SEARCH_DEBOUNCE_DELAY, actionEvent -> {
            LibraryTreeModel model = new LibraryTreeModel(library, node -> {
                String query = getText().trim().toLowerCase();
                return checkQueryMatch(query, node.getName()) ||
                        checkQueryMatch(query, node.getTranslatedName());
            });
            tree.setModelAndRestoreExpansion(model);
            if (!model.isAnyLeafNodeVisible(tree.getExpandedNodes()))
                tree.expandPathTemporarily(new TreePath(model.getFirstLeafParent().getPath()));
        });
        timer.setRepeats(false);

        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter();
            }

            private void filter() {
                timer.restart();
            }
        });

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

    private static boolean checkQueryMatch(String query, String text) {
        // This could be improved to ignore diacritics in certain languages by using java.text.Normalizer.
        return text.toLowerCase().contains(query);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (getText().isEmpty() && !hasFocus()) {
            g.setColor(Color.GRAY);
            g.drawString(Lang.get("key_search"), 5, (getHeight() + getFont().getSize()) / 2);
        }
    }
}
