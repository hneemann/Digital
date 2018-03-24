/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Used to sort the nodes in a more "human typical" order.
 * Sorts the nodes from the input side to the output side.
 */
public class NodeSorter {
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLNode> nodes;

    /**
     * Creates a new instance.
     *
     * @param inputs the initial inputs
     * @param nodes  the nodes to sort
     */
    public NodeSorter(ArrayList<HDLPort> inputs, ArrayList<HDLNode> nodes) {
        this.inputs = inputs;
        this.nodes = nodes;
    }

    /**
     * Performs the sorting and returns a sorted list.
     *
     * @return the sorted list
     */
    public ArrayList<HDLNode> sort() {
        HashSet<HDLNet> nets = new HashSet<>();
        for (HDLPort p : inputs)
            nets.add(p.getNet());

        ArrayList<HDLNode> newOrder = new ArrayList<>();

        // all nodes without an input at top!
        for (HDLNode n : nodes)
            if (n.getInputs().isEmpty()) {
                newOrder.add(n);
                for (HDLPort p : n.getOutputs())
                    if (p.getNet() != null)
                        nets.add(p.getNet());
            }
        nodes.removeAll(newOrder);

        // than a layer sorting
        while (!nodes.isEmpty()) {
            ArrayList<HDLNode> layer = new ArrayList<>();
            for (HDLNode n : nodes) {
                if (dependsOnlyOn(n, nets))
                    layer.add(n);
            }

            if (layer.isEmpty())
                break;

            newOrder.addAll(layer);
            nodes.removeAll(layer);
            for (HDLNode n : layer)
                for (HDLPort p : n.getOutputs())
                    if (p.getNet() != null)
                        nets.add(p.getNet());

        }

        // if there are circular dependencies, keep old order
        if (!nodes.isEmpty())
            newOrder.addAll(nodes);

        return newOrder;
    }

    private boolean dependsOnlyOn(HDLNode n, HashSet<HDLNet> nets) {
        for (HDLPort p : n.getInputs())
            if (!nets.contains(p.getNet()))
                return false;
        return true;
    }
}
