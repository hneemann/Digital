package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.ObservableValue;

/**
 * Represents the state of the state of the inputs and outputs of a part.
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
}
