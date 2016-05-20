package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;

/**
 * Represents the state of the state of the inputs and outputs of a element.
 * It is a simple container bean.
 *
 * @author hneemann
 */
public class IOState {
    private final ObservableValues inputs;
    private final ObservableValues outputs;

    /**
     * creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     */
    public IOState(ObservableValues inputs, ObservableValues outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    /**
     * Returns the input with index i
     * @param i the index
     * @return the input
     */
    public ObservableValue getInput(int i) {
        return inputs.get(i);
    }

    /**
     * Returns the output with index i
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
}
