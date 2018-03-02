/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import java.util.List;

/**
 * Takes an old ordering and order the new items in the same way.
 * The items are compared by calling the {@link OrderMerger#equals(Object, Object)} method.
 *
 * @param <O> type of old items
 * @param <N> type of new items
 */
public class OrderMerger<O, N> {
    private final List<O> oldOrdering;

    /**
     * Creates a new instance
     *
     * @param oldOrdering the old ordering
     */
    public OrderMerger(List<O> oldOrdering) {
        this.oldOrdering = oldOrdering;
    }

    /**
     * Orders the given list
     *
     * @param list the list to order
     * @param <L>  the type of the items
     */
    public <L extends List<N>> void order(L list) {
        if (oldOrdering == null || oldOrdering.size() == 0)
            return;

        int n = 0;
        for (O o : oldOrdering) {
            int found = -1;
            for (int i = n; i < list.size(); i++) {
                if (equals(list.get(i), o)) {
                    found = i;
                    break;
                }
            }
            if (found >= 0) {
                N r = list.remove(found);
                list.add(n, r);
                n++;
            }
        }
    }

    /**
     * Method to compare the items.
     * This implementation simply calls <code>a.equals(b);</code>
     *
     * @param a item
     * @param b item
     * @return true in equal
     */
    public boolean equals(N a, O b) {
        return a.equals(b);
    }
}
