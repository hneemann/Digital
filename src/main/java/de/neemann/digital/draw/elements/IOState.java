package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;

/**
 * Represents the state of the state of the inputs and outputs of a element.
 * It is a simple container bean.
 *
 * @author hneemann
 */
public class IOState {
    private final ObservableValue[] inputs;
    private final ObservableValue[] outputs;

    /**
     * creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     */
    public IOState(ObservableValue[] inputs, ObservableValue[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Returns the input with index i
     * @param i the index
     * @return the input
     */
    public ObservableValue getInput(int i) {
        return inputs[i];
    }

    /**
     * Returns the output with index i
     * @param i the index
     * @return the output
     */
    public ObservableValue getOutput(int i) {
        return outputs[i];
    }

    /**
     * @return the number of inputs
     */
    public int inputCount() {
        return inputs.length;
    }

    /**
     * @return the number of outputs
     */
    public int outputCount() {
        return outputs.length;
    }

    /**
     * @return all inputs as an array
     */
    public ObservableValue[] getInputs() {
        return inputs;
    }

    /**
     * @return all outputs as an array
     */
    public ObservableValue[] getOutputs() {
        return outputs;
    }
}
