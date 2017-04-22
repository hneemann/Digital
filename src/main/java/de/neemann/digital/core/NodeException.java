package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.ImmutableList;
import de.neemann.digital.core.element.PinDescription;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Exception is thrown if the was a problem creating or running the model.
 *
 * @author hneemann
 */
public class NodeException extends Exception {
    private final ArrayList<Node> nodes;
    private final ImmutableList<ObservableValue> values;
    private final int input;

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param values  the values affected by this exception
     */
    public NodeException(String message, ObservableValue... values) {
        this(message, null, -1, new ObservableValues(values));
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param values  the values affected by this exception
     */
    public NodeException(String message, ImmutableList<ObservableValue> values) {
        this(message, null, -1, values);
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param node    the nod effected by tis exception
     * @param input   the affected nodes input
     * @param values  the values affected by this exception
     */
    public NodeException(String message, Node node, int input, ImmutableList<ObservableValue> values) {
        super(message);
        this.input = input;
        this.nodes = new ArrayList<>();
        if (node != null)
            nodes.add(node);
        this.values = values;
    }

    /**
     * Adds a collection of affected nodes to this exception
     *
     * @param nodesToAdd the nodes to add
     * @return this for chained calls
     */
    NodeException addNodes(Collection<Node> nodesToAdd) {
        nodes.addAll(nodesToAdd);
        return this;
    }

    /**
     * returns the affected values.
     *
     * @return the affected values
     */
    public ImmutableList<ObservableValue> getValues() {
        return values;
    }

    @Override
    public String getMessage() {
        ItemConcatenation items = new ItemConcatenation(super.getMessage());
        if (values != null && values.size() > 0) {
            for (ObservableValue ov : values)
                items.addItem(ov.getName());
        }

        if (nodes != null && nodes.size() > 0) {
            HashSet<File> origins = new HashSet<>();
            for (Node node : nodes) {
                if (node != null && node.getOrigin() != null && node.getOrigin().length() > 0)
                    origins.add(node.getOrigin());

                if (node != null)
                    try { // pick the nodes description if available
                        final Field field = node.getClass().getField("DESCRIPTION");
                        Object d = field.get(node);
                        if (d instanceof ElementTypeDescription) {
                            ElementTypeDescription description = (ElementTypeDescription) d;
                            items.addItem(description.getTranslatedName());
                            if (nodes.size() == 1 && input >= 0) {
                                PinDescription in = description.getInput(input);
                                if (in != null)
                                    items.addItem(in.getName());
                            }

                        }
                    } catch (Exception e) {
                        // ignore an error accessing the ElementTypeDescription
                    }
            }
            for (File o : origins)
                items.addItem(o.getName());
        }

        return items.toString();
    }

    /**
     * Returns the affected nodes
     *
     * @return the nodes
     */
    public Collection<Node> getNodes() {
        return nodes;
    }

    private final static class ItemConcatenation {
        private final StringBuilder sb;
        private boolean open;

        private ItemConcatenation(String message) {
            this.sb = new StringBuilder(message);
            open = false;
        }

        private void addItem(String item) {
            if (open)
                sb.append(", ");
            else {
                sb.append(" (");
                open = true;
            }
            sb.append(item);
        }

        @Override
        public String toString() {
            if (open)
                sb.append(")");
            return sb.toString();
        }

    }
}
