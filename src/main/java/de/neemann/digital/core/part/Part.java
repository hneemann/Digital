package de.neemann.digital.core.part;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;

/**
 * A concrete part used for the simulation
 * @author hneemann
 */
public interface Part {
    void setInputs(ObservableValue... inputs) throws NodeException;

    ObservableValue[] getOutputs();

    void registerNodes(Model model);
}
