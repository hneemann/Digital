/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.core.switching.Switch;
import de.neemann.digital.core.wiring.bus.CommonBusValue;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Helper to check a circuit for cycles.
 * A cycle is a situation where a gate input depends somehow on one of its outputs.
 * If a cycle is detected an exception is thrown.
 * A cycle is no problem during simulation due to the gate delay.
 * However, when analyzing a circuit with an unbuffered cycle, erroneous truth tables
 * may result. Therefore, an exception is thrown when such a circuit is analyzed.
 */
public final class CycleDetector {

    private CycleDetector() {
    }

    /**
     * Checks a circuit for cycles
     * If a cycle is detected, en exception is thrown.
     *
     * @param values the input signals of the circuit
     * @throws BacktrackException BacktrackException
     * @throws PinException       PinException
     * @throws CycleException     is thrown if a cycle is detected
     */
    public static void checkForCycles(ArrayList<Signal> values) throws BacktrackException, PinException, CycleException {
        HashMap<NodeInterface, Node> nodes = new HashMap<>();
        HashSet<ObservableValue> visited = new HashSet<>();

        for (Signal s : values) {
            Node root = new Node(null);
            root.layer = 1;
            traverse(root, s.getValue(), nodes, visited);
        }

        // Turned off for now because if it is used you can build circuits with a cycle
        // which are not detected as such.
        //removeSwitchCycles(nodes.values());

        checkGraphForCycles(nodes.values());
    }

    private static void traverse(Node parent, ObservableValue val, HashMap<NodeInterface, Node> nodes, HashSet<ObservableValue> visited) throws PinException, BacktrackException {
        visited.add(val);
        for (Observer o : val.getObservers()) {
            if ((o instanceof NodeInterface)) {

                NodeInterface no = (NodeInterface) o;
                Node child = nodes.computeIfAbsent(no, Node::new);
                child.addParent(parent);

                ObservableValues outputs = ((NodeInterface) o).getOutputs();
                for (ObservableValue co : outputs)
                    if (!visited.contains(co))
                        traverse(child, co, nodes, visited);

            } else
                throw new BacktrackException(Lang.get("err_backtrackOf_N_isImpossible", o.getClass().getSimpleName()));
        }
    }


    private static final class Node {
        private final NodeInterface ni;
        private final ArrayList<Node> parents;
        private int layer;

        private Node(NodeInterface ni) {
            this.parents = new ArrayList<>();
            this.ni = ni;
        }

        private void addParent(Node parent) {
            parents.add(parent);
        }

        @Override
        public String toString() {
            return ni.toString();
        }
    }

    /**
     * Calling this method allows analysis of nmos/cmos circuits because switch cycles are removed.
     * But if this method is called, a cycle which contains a switch in the fed back is not
     * detected anymore.
     *
     * @param nodes the node to analyse
     */
    private static void removeSwitchCycles(Collection<Node> nodes) {
        for (Node n : nodes)
            if (n.ni instanceof CommonBusValue)
                for (Node p : n.parents)
                    if (p.ni instanceof Switch)
                        p.parents.removeIf(node -> node == n);
    }

    private static void checkGraphForCycles(Collection<Node> nodes) throws CycleException {
        ArrayList<Node> remaining = new ArrayList<>(nodes);

        int layer = 1;
        while (!remaining.isEmpty()) {
            layer++;
            ArrayList<Node> ableToPlace = new ArrayList<>();
            for (Node node : remaining) {
                boolean nodeOk = true;
                for (Node p : node.parents)
                    if (p.layer == 0) {
                        nodeOk = false;
                        break;
                    }
                if (nodeOk) {
                    ableToPlace.add(node);
                    node.layer = layer;
                }
            }

            if (ableToPlace.isEmpty())
                throw new CycleException();

            remaining.removeAll(ableToPlace);
        }
    }

    final static class CycleException extends AnalyseException {
        private CycleException() {
            super(Lang.get("err_circuitHasCycles"));
        }
    }
}
