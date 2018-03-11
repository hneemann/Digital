/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Set of rows stored in a special way to make comparisons faster
 */
public final class TableRows implements Iterable<TableRow> {
    private final TreeMap<Long, InnerList> rows;
    private int size;

    /**
     * Creates a new set of table rows
     */
    public TableRows() {
        rows = new TreeMap<>();
    }

    /**
     * @return the number of rows in the table
     */
    public int size() {
        return size;
    }

    /**
     * Adds a row to the table
     *
     * @param tableRow the row to add
     */
    public void add(TableRow tableRow) {
        long flags = tableRow.getOptimizedFlags();
        getList(flags).add(tableRow);
        size++;
    }

    private InnerList getList(long flags) {
        InnerList list = rows.get(flags);
        if (list == null) {
            list = new InnerList();
            rows.put(flags, list);
        }
        return list;
    }

    /**
     * remove all rows from this list
     */
    public void clear() {
        rows.clear();
        size = 0;
    }

    /**
     * Add all rows from the given list
     *
     * @param newRows the list of rows
     */
    public void addAll(TableRows newRows) {
        for (Map.Entry<Long, InnerList> e : newRows.rows.entrySet()) {
            InnerList values = e.getValue();
            getList(e.getKey()).addAll(values);
            size += values.size();
        }
    }

    /**
     * @return truw if this list is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the row stored in this list which is equal to the given row
     *
     * @param r the row to look for
     * @return the row found
     */
    public boolean contains(TableRow r) {
        InnerList list = rows.get(r.getOptimizedFlags());

        if (list == null)
            return false;

        return list.contains(r);
    }

    @Override
    public Iterator<TableRow> iterator() {
        return new RowIterator(rows.values().iterator());
    }

    /**
     * @return a list of lists with all rows of the same optimized bit mask
     */
    public Iterable<InnerList> listIterable() {
        return rows.values();
    }


    /**
     * get the i'th row of this list
     *
     * @param i the index of the row
     * @return the row
     */
    public TableRow get(int i) {
        for (Map.Entry<Long, InnerList> e : rows.entrySet()) {
            InnerList list = e.getValue();
            if (i < list.size())
                return list.get(i);
            else
                i -= list.size();
        }
        throw new IndexOutOfBoundsException();
    }

    private static class RowIterator implements Iterator<TableRow> {
        private final Iterator<InnerList> listIter;
        private Iterator<TableRow> itemIter;

        RowIterator(Iterator<InnerList> iterator) {
            listIter = iterator;
            itemIter = null;
        }

        @Override
        public boolean hasNext() {
            while (itemIter == null && listIter.hasNext()) {
                itemIter = listIter.next().iterator();
                if (!itemIter.hasNext())
                    itemIter = null;
            }
            return itemIter != null;
        }

        @Override
        public TableRow next() {
            TableRow next = itemIter.next();
            if (!itemIter.hasNext())
                itemIter = null;
            return next;
        }
    }

    /**
     * A list of rows with the same optimized mask
     */
    public static final class InnerList implements Iterable<TableRow> {
        private ArrayList<TableRow> innerList;
//        private HashSet<TableRow> innerSet;

        private InnerList() {
            innerList = new ArrayList<>();
//            innerSet=new HashSet<>();
        }

        /**
         * @param r the row to search for
         * @return true if this list contains the given row
         */
        public boolean contains(TableRow r) {
            return innerList.contains(r);
//            return innerSet.contains(r);
        }

        /**
         * Add all given rows to thisd list
         *
         * @param values the rows to add
         */
        public void addAll(InnerList values) {
            for (TableRow tr : values)
                add(tr);
        }

        /**
         * add a single row to this list
         *
         * @param tableRow the row to add
         */
        public void add(TableRow tableRow) {
            innerList.add(tableRow);
//            innerSet.add(tableRow);
        }

        /**
         * @return the size of this list
         */
        public int size() {
            return innerList.size();
        }

        /**
         * returns the i'th element of this list
         *
         * @param i the index
         * @return the row
         */
        public TableRow get(int i) {
            return innerList.get(i);
        }

        @Override
        public Iterator<TableRow> iterator() {
            return innerList.iterator();
        }
    }
}
