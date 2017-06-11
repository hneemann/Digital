package de.neemann.digital.core;

import de.neemann.digital.draw.elements.PinException;

/**
 * A node which has no delay
 * Created by hneemann on 11.06.17.
 */
public abstract class NodeWithoutDelay implements NodeInterface {

    private ObservableValues outputs;

    /**
     * Creates a new instance
     *
     * @param outputs the nodes outputs
     */
    public NodeWithoutDelay(ObservableValue... outputs) {
        this(new ObservableValues(outputs));
    }

    /**
     * Creates a new instance
     *
     * @param outputs the nodes outputs
     */
    public NodeWithoutDelay(ObservableValues outputs) {
        this.outputs = outputs;
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return outputs;
    }
}
