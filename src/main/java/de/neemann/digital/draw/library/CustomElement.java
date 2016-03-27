package de.neemann.digital.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.ModelDescription;
import de.neemann.digital.draw.model.NetList;

/**
 * @author hneemann
 */
public class CustomElement implements Element {

    private final NetList netList;
    private final Circuit circuit;
    private final ElementLibrary library;

    public CustomElement(Circuit circuit, ElementLibrary library) {
        this.circuit = circuit;
        this.library = library;
        netList = new NetList(circuit.getWires());
    }

    public ModelDescription getModelDescription() throws PinException, NodeException {
        return new ModelDescription(circuit, library, true, new NetList(netList));
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
