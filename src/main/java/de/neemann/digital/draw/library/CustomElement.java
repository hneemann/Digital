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
 * This class represents a custom, nested element.
 * So it is possible to use an element in the circuit witch is made from an
 * existing circuit. So you can build hierarchical circuits.
 *
 * @author hneemann
 */
public class CustomElement implements Element {

    private final NetList netList;
    private final Circuit circuit;
    private final ElementLibrary library;
    private final String name;

    /**
     * Creates a new custom element
     *
     * @param circuit the inner circuit
     * @param library the library to use.
     * @param name    the name of the element
     */
    public CustomElement(Circuit circuit, ElementLibrary library, String name) {
        this.circuit = circuit;
        this.library = library;
        this.name = name;
        netList = new NetList(circuit.getWires());
    }

    /**
     * Gets a {@link ModelDescription} of this circuit.
     * Every tim this method is called a new {@link ModelDescription} is created.
     *
     * @return the {@link ModelDescription}
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public ModelDescription getModelDescription() throws PinException, NodeException {
        return new ModelDescription(circuit, library, true, name, new NetList(netList));
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
