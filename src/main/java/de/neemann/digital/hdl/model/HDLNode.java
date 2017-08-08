package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

/**
 * A hdl node.
 * Represents a gate in a circuit
 */
public class HDLNode implements HDLInterface {

    private VisualElement visualElement;
    private Ports ports;

    /**
     * Creates a new instance
     *
     * @param visualElement the element which is represented by this node
     * @param library       the library to use
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws NodeException            NodeException
     * @throws PinException             PinException
     */
    public HDLNode(VisualElement visualElement, ElementLibrary library) throws ElementNotFoundException, NodeException, PinException {
        this.visualElement = visualElement;
        ElementTypeDescription description = library.getElementType(visualElement.getElementName());
        ElementAttributes attr = visualElement.getElementAttributes();
        PinDescriptions inputs = description.getInputDescription(attr);
        PinDescriptions outputs = description.getOutputDescriptions(attr);
        Element element = description.createElement(attr);
        ObservableValues outputValues = element.getOutputs();

        ports = new Ports();
        for (int i = 0; i < outputs.size(); i++) {
            Port port = new Port(outputs.get(i).getName(), Port.Direction.out);
            port.setBits(outputValues.get(i).getBits());
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
            if (port.getName().equalsIgnoreCase(pin.getName())) {
                signal.addPort(port);
                return;
            }

        throw new HDLException(Lang.get("err_pin_N_notFound", pin.getName()));
    }
}
