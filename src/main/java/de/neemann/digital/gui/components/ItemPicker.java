/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Shows a list of items. Its a kind of a quick selection mode. The user can press ESC
 * to make no selection or move the cursor and select an item by pressing ENTER.
 *
 * @param <T> the type of the items
 */
public class ItemPicker<T> extends JDialog {
    private final JList<T> list;
    private final java.util.List<T> items;
    private T selectedEntry = null;

    /**
     * Creates an new instance
     *
     * @param parent the parent frame
     * @param items  the list of items
     */
    public ItemPicker(Window parent, final java.util.List<T> items) {
        this(parent, Lang.get("win_itempicker_title"), items);
    }

    /**
     * Creates an new instance
     *
     * @param parent the parent frame
     * @param title  title of the dialog
     * @param items  the list of items
     */
    public ItemPicker(Window parent, final String title, final java.util.List<T> items) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.items = items;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        list = new JList<T>(new MyListModel<T>(items));
        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "myESC");
        list.getActionMap().put("myESC", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        list.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "myENTER");
        list.getActionMap().put("myENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSelectedAsResult();
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    setSelectedAsResult();
                }
            }
        });

        getContentPane().add(new JScrollPane(list));

        pack();
        setLocationRelativeTo(parent);
    }

    private void setSelectedAsResult() {
        int index = list.getSelectedIndex();
        if (index >= 0)
            selectedEntry = items.get(index);
        dispose();
    }

    /**
     * Shows the modal dialog
     *
     * @return true if user has a selection made
     */
    public boolean showDialog() {
        setVisible(true);
        return selectedEntry != null;
    }

    /**
     * @return return the selected item
     */
    public T getSelected() {
        return selectedEntry;
    }

    /**
     * Selects an entry
     *
     * @return the selected entry or null if nothing selected
     */
    public T select() {
        if (showDialog())
            return getSelected();
        else
            return null;
    }

    private static final class MyListModel<T> implements ListModel<T> {
        private final java.util.List<T> items;

        private MyListModel(java.util.List<T> items) {
            this.items = items;
        }

        @Override
        public int getSize() {
            return items.size();
        }

        @Override
        public T getElementAt(int index) {
            return items.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
        }
    }
}
