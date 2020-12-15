/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.lang.Lang;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Orders items.
 * The items are ordered in their container described by {@link ElementOrderer.OrderInterface}.
 *
 * @param <T> the element types to order
 */
public class ElementOrderer<T> extends JDialog {

    private final JPanel buttons;
    private final JList<T> list;
    private final MyListModel<T> listModel;
    private final OrderInterface<T> data;

    private boolean okPressed = true;

    /**
     * Creates a new instance
     *
     * @param owner the owner of this dialog
     * @param title the dialogs title
     * @param data  the data to order
     */
    public ElementOrderer(Window owner, String title, OrderInterface<T> data) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        this.data = data;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        listModel = new MyListModel<T>(data);
        list = new JList<T>(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        JScrollPane scrollPane = new JScrollPane(list);
        getContentPane().add(scrollPane);
        scrollPane.setPreferredSize(Screen.getInstance().scale(new Dimension(100, 150)));

        buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(new ToolTipAction("\u2191") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i0 = list.getSelectionModel().getMinSelectionIndex();
                int i1 = list.getSelectionModel().getMaxSelectionIndex();
                if (i0 > 0 && i1 > 0) {
                    for (int i = i0; i <= i1; i++)
                        listModel.swap(i, i - 1);
                    list.getSelectionModel().setSelectionInterval(i0 - 1, i1 - 1);
                }
            }
        }.setToolTip(Lang.get("tt_moveItemUp")).createJButton());
        buttons.add(new ToolTipAction("\u2193") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i0 = list.getSelectionModel().getMinSelectionIndex();
                int i1 = list.getSelectionModel().getMaxSelectionIndex();
                if (i0 >= 0 && i0 < data.size() - 1 && i1 >= 0 && i1 < data.size() - 1) {
                    for (int i = i1; i >= i0; i--)
                        listModel.swap(i, i + 1);
                    list.getSelectionModel().setSelectionInterval(i0 + 1, i1 + 1);
                }
            }
        }.setToolTip(Lang.get("tt_moveItemDown")).createJButton());
        getContentPane().add(buttons, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * Called to add a ok button
     *
     * @return this for chained calls
     */
    public ElementOrderer<T> addOkButton() {
        JButton okButton = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed = true;
                dispose();
            }
        });
        getContentPane().add(okButton, BorderLayout.SOUTH);
        okPressed = false;
        return this;
    }

    /**
     * Called to add a delete button
     *
     * @param minEntries the min number of entries
     * @return this for chained calls
     */
    public ElementOrderer<T> addDeleteButton(final int minEntries) {
        buttons.add(new ToolTipAction("\u2717") { // 274C is not visible on Windows, 2715,2716,2717,2718 works an linux
            @Override
            public void actionPerformed(ActionEvent e) {
                int i0 = list.getSelectionModel().getMinSelectionIndex();
                int i1 = list.getSelectionModel().getMaxSelectionIndex();
                int del = i1 - i0 + 1;
                if (data.size() - del >= minEntries && del > 0)
                    for (int i = i1; i >= i0; i--)
                        listModel.delete(i);
                if (data.size() <= minEntries)
                    setEnabled(false);
            }
        }.setToolTip(Lang.get("tt_deleteItem")).setEnabledChain(data.size() > minEntries).createJButton());
        return this;
    }

    /**
     * Shows the dialog
     *
     * @return true if ok was pressed
     */
    public boolean showDialog() {
        pack();
        setVisible(true);
        return okPressed;
    }

    /**
     * Container for the items to order
     *
     * @param <T> the type of the items
     */
    public interface OrderInterface<T> {

        /**
         * @return number of items
         */
        int size();

        /**
         * Returns the requested items
         *
         * @param index the index
         * @return the item
         */
        T get(int index);

        /**
         * Swap the items
         *
         * @param i th item
         * @param j th item
         */
        void swap(int i, int j);

        /**
         * Deletes the given item
         *
         * @param index the element to delete
         */
        default void delete(int index) {
            throw new UnsupportedOperationException("delete");
        }

    }

    /**
     * Implementation to order a list
     *
     * @param <T> type of items
     */
    public static class ListOrder<T> implements OrderInterface<T> {
        private java.util.List<T> list;

        /**
         * Creates a new instance
         *
         * @param list the list to order
         */
        public ListOrder(java.util.List<T> list) {
            this.list = list;
        }

        @Override
        public int size() {
            return list.size();
        }

        @Override
        public T get(int index) {
            return list.get(index);
        }

        @Override
        public void swap(int i, int j) {
            T z = list.get(i);
            list.set(i, list.get(j));
            list.set(j, z);
        }

        @Override
        public void delete(int index) {
            list.remove(index);
        }
    }

    private class MyListModel<T> implements ListModel<T> {
        private final OrderInterface<T> data;
        private ArrayList<ListDataListener> listener;

        MyListModel(OrderInterface<T> data) {
            this.data = data;
            listener = new ArrayList<>();
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public T getElementAt(int index) {
            return data.get(index);
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listener.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listener.remove(l);
        }

        void swap(int i, int j) {
            data.swap(i, j);
            fireEvent(Math.min(i, j), Math.max(i, j));
        }

        void delete(int i) {
            data.delete(i);
            fireEventDeleted(i);
        }

        private void fireEvent(int min, int max) {
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, min, max);
            for (ListDataListener l : listener)
                l.contentsChanged(e);
        }

        private void fireEventDeleted(int item) {
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, item, item);
            for (ListDataListener l : listener)
                l.contentsChanged(e);
        }

    }
}
