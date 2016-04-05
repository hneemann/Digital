package de.neemann.digital.core;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author hneemann
 */
public class NodeException extends Exception {
    private final ArrayList<Node> nodes;
    private final ObservableValue[] values;

    public NodeException(String message, ObservableValue... values) {
        this(message, null, values);
    }

    public NodeException(String message, Node node, ObservableValue... values) {
        super(message);
        this.nodes = new ArrayList<>();
        if (node != null)
            nodes.add(node);
        this.values = values;
    }

    public NodeException addNodes(Collection<Node> nodesToAdd) {
        nodes.addAll(nodesToAdd);
        return this;
    }

    public ObservableValue[] getValues() {
        return values;
    }

    @Override
    public String getMessage() {
        if (values == null || values.length == 0)
            return super.getMessage();
        else {
            StringBuilder sb = new StringBuilder(super.getMessage());
            sb.append(": ");
            boolean first = true;
            for (ObservableValue ov : values) {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                sb.append(ov.getName());
            }
            return sb.toString();
        }
    }

    public Collection<Node> getNodes() {
        return nodes;
    }
}
