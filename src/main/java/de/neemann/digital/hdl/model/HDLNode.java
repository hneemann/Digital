package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.CustomElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

/**
 * A hdl node.
 * Represents a gate in a circuit
 */
public class HDLNode implements HDLInterface {

    private final boolean isCustom;
    private VisualElement visualElement;
    private Ports ports;

    /**
     * Creates a new instance
     *
     * @param visualElement the element which is represented by this node
     * @param library       the library to use
     * @param modelList     used to load nested models
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws NodeException            NodeException
     * @throws PinException             PinException
     * @throws HDLException             HDLException
     */
    public HDLNode(VisualElement visualElement, ElementLibrary library, ModelList modelList) throws ElementNotFoundException, NodeException, PinException, HDLException {
        this.visualElement = visualElement;
        ElementTypeDescription description = library.getElementType(visualElement.getElementName());
        ElementAttributes attr = visualElement.getElementAttributes();
        PinDescriptions inputs = description.getInputDescription(attr);
        PinDescriptions outputs = description.getOutputDescriptions(attr);
        Element element = description.createElement(attr);
        BitProvider bitProvider;
        isCustom = element instanceof CustomElement;
        if (isCustom) {
            HDLModel model = modelList.getModel(((CustomElement) element).getCircuit(), visualElement.getElementName());
            bitProvider = model::getOutputBits;
        } else {
            final ObservableValues o = element.getOutputs();
            bitProvider = i -> o.get(i).getBits();
        }

        ports = new Ports();
        for (int i = 0; i < outputs.size(); i++) {
            Port port = new Port(outputs.get(i).getName(), Port.Direction.out);
            port.setBits(bitProvider.getBits(i));
            ports.add(port);

        }
        for (PinDescription in : inputs)
            ports.add(new Port(in.getName(), Port.Direction.in));
    }

    @Override
    public Ports getPorts() {
        return ports;
    }

    /**
     * @return the represented element
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }

    /**
     * Connects a pin to a signal
     *
     * @param pin    the pin
     * @param signal the signals
     * @throws HDLException HDLException
     */
    public void setPinToSignal(Pin pin, Signal signal) throws HDLException {
        for (Port port : ports)
            if (port.getOrigName().equalsIgnoreCase(pin.getName())) {
                signal.addPort(port);
                return;
            }

        throw new HDLException(Lang.get("err_pin_N_notFound", pin.getName()));
    }

    /**
     * tests if this node represents a element of the given type
     *
     * @param description the element
     * @return true if node represents such an element
     */
    public boolean is(ElementTypeDescription description) {
        return visualElement.equalsDescription(description);
    }

    private interface BitProvider {
        int getBits(int i);
    }

    /**
     * @return true if this is a custom node
     */
    public boolean isCustom() {
        return isCustom;
    }

    /**
     * return a attribute value
     *
     * @param key the key
     * @param <V> the type of the key
     * @return the value
     */
    public <V> V get(Key<V> key) {
        return visualElement.getElementAttributes().get(key);
    }
}
