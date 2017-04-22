package de.neemann.digital.core;

import de.neemann.digital.core.element.ImmutableList;

/**
 * Is thrown if bit count is not matching
 *
 * @author hneemann
 */
public class BitsException extends NodeException {

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
