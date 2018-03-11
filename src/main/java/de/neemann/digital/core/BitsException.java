/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.core.element.ImmutableList;

/**
 * Is thrown if bit count is not matching
 */
public class BitsException extends NodeException {

    /**
     * Creates a new instance.
     * Avoid to use this constructor because it provides no further information to fix the circuit.
     * Use only if it's to expensive to provide more information. Try to provide at least a node.
     *
     * @param message the message
     */
    public BitsException(String message) {
        super(message, null, -1, null);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param node    the affected node
     */
    public BitsException(String message, Node node) {
        super(message, node, -1, null);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param values  the affected values
     */
    public BitsException(String message, ImmutableList<ObservableValue> values) {
        super(message, null, -1, values);
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param node    the affected node
     * @param input   the affected nodes input
     * @param values  the affected values
     */
    public BitsException(String message, Node node, int input, ObservableValue... values) {
        super(message, node, input, new ObservableValues(values));
    }

    /**
     * Creates a new instance
     *
     * @param message the message
     * @param node    the affected node
     * @param input   the affected nodes input
     * @param values  the affected values
     */
    public BitsException(String message, Node node, int input, ImmutableList<ObservableValue> values) {
        super(message, node, input, values);
    }
}
