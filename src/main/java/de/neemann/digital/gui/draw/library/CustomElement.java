package de.neemann.digital.gui.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.gui.draw.elements.Circuit;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.model.ModelDescription;

/**
 * @author hneemann
 */
public class CustomElement implements Element {

    private Circuit circuit;
    private ElementLibrary library;

    public CustomElement(Circuit circuit, ElementLibrary library) {
        this.circuit = circuit;
        this.library = library;
    }

    public ModelDescription getModelDescription() throws PinException {
        return new ModelDescription(circuit, library, true);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new RuntimeException("invalid call!");
    }

    @Override
    public ObservableValue[] getOutputs() {
        try {
            return circuit.getOutputNames(library);
        } catch (PinException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerNodes(Model model) {
        throw new RuntimeException("invalid call!");
    }
}
