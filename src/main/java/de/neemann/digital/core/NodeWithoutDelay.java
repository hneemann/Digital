/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.draw.elements.PinException;

/**
 * A node which has no delay
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
