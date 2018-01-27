package de.neemann.digital.analyse;

import de.neemann.digital.core.*;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Helper to check a circuit for circles
 */
public final class CircleDetector {

    private CircleDetector() {
    }

    /**
     * Returns true if the circuit has circles.
     *
     * @param values the input signals of the circuit
     * @return if the circuit has circles
     * @throws BacktrackException BacktrackException
     * @throws PinException       PinException
     */
    public static boolean hasCircles(ArrayList<Signal> values) throws BacktrackException, PinException {
        HashMap<NodeInterface, Node> nodes = new HashMap<>();
        HashSet<ObservableValue> visited = new HashSet<>();

        for (Signal s : values) {
            Node root = new Node(null);
            root.layer = 1;
            traverse(root, s.getValue(), nodes, visited);
        }

        try {
            checkForCircles(nodes.values());
            return false;
        } catch (CircleException e) {
            return true;
        }
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

    private static void checkForCircles(Collection<Node> nodes) throws CircleException {
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
                throw new CircleException();

            remaining.removeAll(ableToPlace);
        }
    }

    private static class CircleException extends Exception {
    }
}
