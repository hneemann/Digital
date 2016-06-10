package de.neemann.digital.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
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

    private final Circuit circuit;
    private final ElementLibrary library;
    private final String name;
    private NetList netList;

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
    }

    /**
     * Gets a {@link ModelDescription} of this circuit.
     * Every time this method is called a new {@link ModelDescription} is created.
     *
     * @param subName name of the circuit, used to name unique elements
     * @return the {@link ModelDescription}
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public ModelDescription getModelDescription(String subName) throws PinException, NodeException {
        if (netList == null)
            netList = new NetList(circuit);

        return new ModelDescription(circuit, library, true, name, new NetList(netList), subName);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new RuntimeException("invalid call!");
    }

    @Override
    public ObservableValues getOutputs() {
        try {
            return circuit.getOutputNames();
        } catch (PinException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerNodes(Model model) {
        throw new RuntimeException("invalid call!");
    }
}
