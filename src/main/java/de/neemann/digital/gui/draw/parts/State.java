package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.core.ObservableValue;

/**
 * @author hneemann
 */
public class State {
    private final ObservableValue[] inputs;
    private final ObservableValue[] outputs;

    public State(ObservableValue[] inputs, ObservableValue[] outputs) {
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
