/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.ImmutableList;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * This exception is thrown if there was a problem creating or running the model.
 * Call one of the constructors with as much information as possible to create
 * a useful error message.
 */
public class NodeException extends ExceptionWithOrigin {
    private final ArrayList<Node> nodes;
    private final ImmutableList<ObservableValue> values;
    private final int input;

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param cause   the cause of this exception
     */
    public NodeException(String message, Exception cause) {
        this(message, null, -1, null, cause);
    }

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
        this(message, node, input, values, null);
    }

    /**
     * Creates a new instance.
     *
     * @param message the message
     * @param node    the nod effected by tis exception
     * @param input   the affected nodes input
     * @param values  the values affected by this exception
     * @param cause   the cause
     */
    public NodeException(String message, Node node, int input, ImmutableList<ObservableValue> values, Exception cause) {
        super(message, cause);
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
                items.addItem(Lang.get("msg_signal_N", ov.getName()));
        }

        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
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
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        // ignore an error accessing the ElementTypeDescription
                    }
            }
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
        private final String message;
        private final HashMap<String, Item> items;

        private ItemConcatenation(String message) {
            this.message = message;
            items = new HashMap<>();
        }

        private void addItem(String item) {
            Item it = items.computeIfAbsent(item, Item::new);
            it.incUsage();
        }

        @Override
        public String toString() {
            if (items.isEmpty())
                return message;

            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Item e : items.values()) {
                if (first)
                    first = false;
                else
                    sb.append(", ");
                sb.append(e);
            }

            return message + "\n" + Lang.get("msg_affectedComponentsAre_N", sb.toString());
        }
    }

    private final static class Item {
        private final String item;
        private int usage;

        private Item(String item) {
            this.item = item;
        }

        private void incUsage() {
            usage++;
        }

        @Override
        public String toString() {
            if (usage == 1)
                return item;
            else
                return usage + "*" + item;
        }
    }

    @Override
    public Set<File> getOrigin() {
        Set<File> o = super.getOrigin();
        if (o != null)
            return o;

        HashSet<File> originSet = null;
        for (Node n : nodes)
            if (n.getOrigin() != null) {
                if (originSet == null) originSet = new HashSet<>();
                originSet.add(n.getOrigin());
            }
        return originSet;
    }

}
