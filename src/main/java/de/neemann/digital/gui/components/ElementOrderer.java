package de.neemann.digital.gui.components;

import de.neemann.digital.lang.Lang;
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
 * @author hneemann
 */
public class ElementOrderer<T> extends JDialog {

    /**
     * Creates a new instance
     *
     * @param owner the owner of this dialog
     * @param title the dialogs title
     * @param data  the data to order
     */
    public ElementOrderer(Frame owner, String title, OrderInterface<T> data) {
        super(owner, title, true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        MyListModel<T> listModel = new MyListModel<T>(data);
        JList list = new JList<T>(listModel);
        getContentPane().add(new JScrollPane(list));

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(new ToolTipAction("\u2191") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = list.getSelectedIndex();
                if (i > 0) {
                    listModel.swap(i, i - 1);
                    list.setSelectedIndex(i - 1);
                }
            }
        }.setToolTip(Lang.get("tt_moveItemUp")).createJButton());
        buttons.add(new ToolTipAction("\u2193") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = list.getSelectedIndex();
                if (i >= 0 && i < data.size() - 1) {
                    listModel.swap(i, i + 1);
                    list.setSelectedIndex(i + 1);
                }

            }
        }.setToolTip(Lang.get("tt_moveItemDown")).createJButton());
        getContentPane().add(buttons, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(owner);
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
    }

    private static class ArrayOrder<T> implements OrderInterface<T> {
        private final T[] data;

        ArrayOrder(T[] data) {
            this.data = data;
        }

        @Override
        public int size() {
            return data.length;
        }

        @Override
        public T get(int index) {
            return data[index];
        }

        @Override
        public void swap(int i, int j) {
            T x = data[i];
            data[i] = data[j];
            data[j] = x;
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

        private void fireEvent(int min, int max) {
            ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, min, max);
            for (ListDataListener l : listener)
                l.contentsChanged(e);
        }
    }
}
