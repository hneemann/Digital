package de.neemann.digital.gui.draw.elements;

import de.neemann.digital.core.ObservableValue;

/**
 * Represents the state of the state of the inputs and outputs of a element.
 *
 * @author hneemann
 */
public class IOState {
    private final ObservableValue[] inputs;
    private final ObservableValue[] outputs;

    public IOState(ObservableValue[] inputs, ObservableValue[] outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public ObservableValue getInput(int i) {
        return inputs[i];
    }

    public ObservableValue getOutput(int i) {
        return outputs[i];
    }

    public int inputCount() {
        return inputs.length;
    }

    public int outputCount() {
        return outputs.length;
    }

    public ObservableValue[] getInputs() {
        return inputs;
    }

    public ObservableValue[] getOutputs() {
        return outputs;
    }
}
