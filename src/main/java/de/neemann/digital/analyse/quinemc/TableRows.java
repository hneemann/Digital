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
final class TableRows implements Iterable<TableRow> {
    private final TreeMap<Long, InnerList> rows;
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

    private InnerList getList(long flags) {
        InnerList list = rows.get(flags);
        if (list == null) {
            list = new InnerList();
            rows.put(flags, list);
        }
        return list;
    }

    public void clear() {
        rows.clear();
        size = 0;
    }

    public void addAll(TableRows newRows) {
        for (Map.Entry<Long, InnerList> e : newRows.rows.entrySet()) {
            InnerList values = e.getValue();
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

    public Iterable<InnerList> listIterable() {
        return rows.values();
    }


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

    static final class InnerList implements Iterable<TableRow> {
        private ArrayList<TableRow> innerList;
//        private HashSet<TableRow> innerSet;

        private InnerList() {
            innerList=new ArrayList<>();
//            innerSet=new HashSet<>();
        }

        public boolean contains(TableRow r) {
            return innerList.contains(r);
//            return innerSet.contains(r);
        }

        public void addAll(InnerList values) {
            for (TableRow tr : values)
                add(tr);
        }

        public void add(TableRow tableRow) {
            innerList.add(tableRow);
//            innerSet.add(tableRow);
        }

        public int size() {
            return innerList.size();
        }

        public TableRow get(int i) {
            return innerList.get(i);
        }

        public Iterator<TableRow> iterator() {
            return innerList.iterator();
        }
    }
}
