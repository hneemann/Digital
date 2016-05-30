package de.neemann.digital.analyse.quinemc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Set of rows stored in a special way to make comparisons faster
 *
 * @author hneemann
 */
class TableRows implements Iterable<TableRow> {
    private final TreeMap<Long, ArrayList<TableRow>> rows;
    private int size;

    TableRows() {
        rows = new TreeMap<>();
    }

    public int size() {
        return size;
    }

    public void add(TableRow tableRow) {
        long flags = tableRow.getOptimizedFlags();
        getList(flags).add(tableRow);
        size++;
    }

    private ArrayList<TableRow> getList(long flags) {
        ArrayList<TableRow> list = rows.get(flags);
        if (list == null) {
            list = new ArrayList<>();
            rows.put(flags, list);
        }
        return list;
    }

    public void clear() {
        rows.clear();
        size = 0;
    }

    public void addAll(TableRows newRows) {
        for (Map.Entry<Long, ArrayList<TableRow>> e : newRows.rows.entrySet()) {
            ArrayList<TableRow> values = e.getValue();
            getList(e.getKey()).addAll(values);
            size += values.size();
        }
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the row stored in this list which is equal to the given row
     *
     * @param r the row to look for
     * @return the row found
     */
    TableRow findRow(TableRow r) {
        ArrayList<TableRow> list = rows.get(r.getOptimizedFlags());

        if (list == null)
            return null;

        int i = list.indexOf(r);
        if (i < 0)
            return null;
        else
            return list.get(i);
    }

    @Override
    public Iterator<TableRow> iterator() {
        return new RowIterator(rows.values().iterator());
    }

    public Iterable<ArrayList<TableRow>> listIterable() {
        return rows.values();
    }


    public TableRow get(int i) {
        for (Map.Entry<Long, ArrayList<TableRow>> e : rows.entrySet()) {
            ArrayList<TableRow> list = e.getValue();
            if (i < list.size())
                return list.get(i);
            else
                i -= list.size();
        }
        throw new IndexOutOfBoundsException();
    }

    private class RowIterator implements Iterator<TableRow> {
        private final Iterator<ArrayList<TableRow>> listIter;
        private Iterator<TableRow> itemIter;

        RowIterator(Iterator<ArrayList<TableRow>> iterator) {
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
}
