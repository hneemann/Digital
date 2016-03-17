package de.neemann.digital.core;

/**
 * @author hneemann
 */
public interface Part {
    void setInputs(ObservableValue... inputs) throws NodeException;

    ObservableValue[] getOutputs();

    void registerNodes(Model model);
}
