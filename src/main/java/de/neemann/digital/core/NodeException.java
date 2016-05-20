package de.neemann.digital.core;

import de.neemann.digital.core.element.UnmutableList;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Exception is thrown if the was a problem creating or running the model.
 *
 * @author hneemann
 */
public class NodeException extends Exception {
    private final ArrayList<Node> nodes;
    private final UnmutableList<ObservableValue> values;

    /**
     * Creates a new instance.
     *
     * @param message the message
     */
    public NodeException(String message, ObservableValue... values) {
        this(message, null, new ObservableValues(values));
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param values  the values affected by this exception
     */
    public NodeException(String message, UnmutableList<ObservableValue> values) {
        this(message, null, values);
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param node    the nod effected by tis exception
     * @param values  the values affected by this exception
     */
    public NodeException(String message, Node node, UnmutableList<ObservableValue> values) {
        super(message);
        this.nodes = new ArrayList<>();
        if (node != null)
            nodes.add(node);
        this.values = values;
    }

    /**
     * Addes a collection of accefted nodes to this exception
     *
     * @param nodesToAdd the nodes to add
     * @return this for chained calls
     */
    public NodeException addNodes(Collection<Node> nodesToAdd) {
        nodes.addAll(nodesToAdd);
        return this;
    }

    /**
     * returns the affected values.
     *
     * @return the affected values
     */
    public UnmutableList<ObservableValue> getValues() {
        return values;
    }

    @Override
    public String getMessage() {
        if (values == null || values.size() == 0)
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

    /**
     * Returns the affected nodes
     *
     * @return the nodes
     */
    public Collection<Node> getNodes() {
        return nodes;
    }
}
