/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;

/**
 * Represents the state of the state of the inputs and outputs of a element.
 * It is a simple container bean.
 */
public class IOState {
    private final ObservableValues inputs;
    private final ObservableValues outputs;
    private final Element element;

    /**
     * creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param element the element represented
     */
    public IOState(ObservableValues inputs, ObservableValues outputs, Element element) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.element = element;
    }

    /**
     * Returns the input with index i
     *
     * @param i the index
     * @return the input
     */
    public ObservableValue getInput(int i) {
        return inputs.get(i);
    }

    /**
     * Returns the output with index i
     *
     * @param i the index
     * @return the output
     */
    public ObservableValue getOutput(int i) {
        return outputs.get(i);
    }

    /**
     * @return the number of inputs
     */
    public int inputCount() {
        return inputs.size();
    }

    /**
     * @return the number of outputs
     */
    public int outputCount() {
        return outputs.size();
    }

    /**
     * @return all inputs as an array
     */
    public ObservableValues getInputs() {
        return inputs;
    }

    /**
     * @return all outputs as an array
     */
    public ObservableValues getOutputs() {
        return outputs;
    }

    /**
     * @return the element which this state belongs to
     */
    public Element getElement() {
        return element;
    }
}
