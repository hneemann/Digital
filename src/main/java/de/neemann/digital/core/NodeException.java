package de.neemann.digital.core;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author hneemann
 */
public class NodeException extends Exception {
    private final Node node;
    private final ObservableValue[] values;

    public NodeException(String message, Node node, ObservableValue... values) {
        super(message);
        this.node = node;
        this.values = values;
    }

    public ObservableValue[] getValues() {
        return values;
    }

    public Node getNode() {
        return node;
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
        if (node == null)
            return null;

        ArrayList<Node> list = new ArrayList<>();
        list.add(node);
        return list;
    }
}
