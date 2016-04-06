package de.neemann.digital.gui.components;

import java.util.List;

/**
 * @author hneemann
 */
public class OrderMerger<O, N> {
    private final List<O> oldOrdering;

    public OrderMerger(List<O> oldOrdering) {
        this.oldOrdering = oldOrdering;
    }

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

    public boolean equals(N a, O b) {
        return a.equals(b);
    }
}
