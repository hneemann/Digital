package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.pld.PullDown;
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
    private final VisualElement visualElement;
    private final ElementAttributes attr;
    private final String name;
    private String hdlName;
    private Ports ports;

    /**
     * Creates a new node
     *
     * @param ports the ports of the node
     * @param name  the name of this node
     * @param attr  the attributes of the node
     * @throws HDLException if name is not valid
     */
    public HDLNode(Ports ports, String name, ElementAttributes attr) throws HDLException {
        this.attr = attr;
        this.isCustom = false;
        this.visualElement = null;
        this.ports = ports;
        this.name = name;
        this.hdlName = Port.getHDLName(name);
    }

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
        this.attr = visualElement.getElementAttributes();
        this.name = visualElement.getElementName();
        hdlName = Port.getHDLName(this.name);
        ElementTypeDescription description = library.getElementType(visualElement.getElementName());
        ElementAttributes attr = visualElement.getElementAttributes();
        PinDescriptions inputs = description.getInputDescription(attr);
        PinDescriptions outputs = description.getOutputDescriptions(attr);
        Element element = description.createElement(attr);
        BitProvider bitProvider;
        isCustom = element instanceof CustomElement;
        if (isCustom) {
            HDLModel model = modelList.getModel(((CustomElement) element).getCircuit(), visualElement.getElementName());
            bitProvider = i -> new ValueModel(model, i);
        } else {
            final ObservableValues o = element.getOutputs();
            bitProvider = i -> new ValueObservableModel(o, i);
        }

        ports = new Ports();
        for (int i = 0; i < outputs.size(); i++) {
            Port port = new Port(outputs.get(i).getName(), Port.Direction.out, outputs.get(i).getDescription());
            Value value = bitProvider.getValue(i);
            port.setBits(value.getBits());
            if (value.isConstant())
                port.setConstant(value.getConstant());

            ports.add(port);

        }
        for (PinDescription in : inputs)
            ports.add(new Port(in.getName(), Port.Direction.in, in.getDescription()));
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
     * @return the attributes of this element
     */
    public ElementAttributes getAttributes() {
        return attr;
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
        return description.getName().equals(name);
    }

    /**
     * @return the name of this node
     */
    public String getHDLName() {
        return hdlName;
    }

    /**
     * Sets the hdl name of this node
     *
     * @param hdlName the hdl name
     */
    public void setHDLName(String hdlName) {
        this.hdlName = hdlName;
    }

    /**
     * @return the original name of the node
     */
    public String getOrigName() {
        return name;
    }

    private interface BitProvider {
        Value getValue(int i);
    }

    private interface Value {

        int getBits();

        boolean isConstant();

        HDLConstant getConstant() throws HDLException;
    }

    private static final class ValueModel implements Value {
        private final HDLModel model;
        private final int i;

        private ValueModel(HDLModel model, int i) {
            this.model = model;
            this.i = i;
        }

        @Override
        public int getBits() {
            return model.getOutputBits(i);
        }

        @Override
        public boolean isConstant() {
            return false;
        }

        @Override
        public HDLConstant getConstant() throws HDLException {
            throw new HDLException("invalid call");
        }
    }

    private static final class ValueObservableModel implements Value {
        private final ObservableValues observableValues;
        private final int i;

        private ValueObservableModel(ObservableValues observableValues, int i) {
            this.observableValues = observableValues;
            this.i = i;
        }

        @Override
        public int getBits() {
            return observableValues.get(i).getBits();
        }

        @Override
        public boolean isConstant() {
            return observableValues.get(i).isConstant() || observableValues.get(i) instanceof PullDown.PullObservableValue;
        }

        @Override
        public HDLConstant getConstant() throws HDLException {
            int bits = observableValues.get(i).getBits();
            if (observableValues.get(i) instanceof PullDown.PullObservableValue) {
                PullDown.PullObservableValue obs = (PullDown.PullObservableValue) observableValues.get(i);
                switch (obs.getPullResistor()) {
                    case pullUp:
                        return new HDLConstant(HDLConstant.Type.weakHigh, bits);
                    case pullDown:
                        return new HDLConstant(HDLConstant.Type.weakLow, bits);
                    default:
                        throw new HDLException("undefined pull resistor type");
                }
            } else
                return new HDLConstant(observableValues.get(i).getValue(), bits);
        }
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
        return attr.get(key);
    }

    @Override
    public String toString() {
        return name;
    }
}
