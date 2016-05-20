package de.neemann.digital.draw.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The ModelEntry is used to generate the model.
 * It holds the element which is able to register nodes to the model and the visual element which is
 * the visual representation of the circuits.
 *
 * @author hneemann
 */
public class ModelEntry {
    private final Element element;
    private final Pins pins;
    private final PinDescriptions inputNames;
    private final boolean isNestedElement;
    private final VisualElement visualElement;
    private IOState ioState;

    /**
     * Creates a new instance
     *
     * @param element         the element which is created
     * @param pins            the pins transformed in the circuits coordinate system
     * @param visualElement   the visual element which has created the element
     * @param inputNames      the pin descriptions of the inputs.
     * @param isNestedElement true if this visual element is a nested included element
     */
    public ModelEntry(Element element, Pins pins, VisualElement visualElement, PinDescriptions inputNames, boolean isNestedElement) {
        this.element = element;
        this.pins = pins;
        this.visualElement = visualElement;
        this.inputNames = inputNames;
        this.isNestedElement = isNestedElement;
    }

    /**
     * Sets the Inputs of the element contained in this entry
     *
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public void applyInputs() throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        ObservableValue[] inputs = new ObservableValue[inputNames.size()];
        ObservableValues values = new ObservableValues(inputs);
        if (inputNames.size() > 0) {
            for (int i = 0; i < inputNames.size(); i++) {
                Pin pin = ins.get(inputNames.get(i).getName());
                if (pin == null)
                    throw new PinException(Lang.get("err_pin_N0_atElement_N1_notFound", inputNames.get(i), visualElement), visualElement);

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException(Lang.get("err_noValueSetFor_N0_atElement_N1", inputNames.get(i), visualElement), visualElement);

                inputs[i] = value;
            }

            ArrayList<ObservableValue> bidirect = null;
            for (Pin p : pins) {
                if (p.getDirection() == Pin.Direction.both) {
                    if (bidirect == null)
                        bidirect = new ArrayList<>();
                    bidirect.add(p.getReaderValue());
                }
            }
            if (bidirect != null)
                values = new ObservableValues(Splitter.combine(inputs, bidirect.toArray(new ObservableValue[bidirect.size()])));

            element.setInputs(values);
        }
        ioState = new IOState(values, element.getOutputs());
    }

    /**
     * Connects this model to the gui.
     *
     * @param guiObserver the observer which could be notified if the a repaint is necessary
     */
    public void connectToGui(Observer guiObserver) {
        if (!isNestedElement) {
            if (ioState == null)
                throw new RuntimeException("call applyInputs before connectToGui");
            visualElement.setState(ioState, guiObserver);
        }
    }

    /**
     * @return the pins of this model entry
     */
    public Pins getPins() {
        return pins;
    }

    /**
     * @return the element of this entry
     */
    public Element getElement() {
        return element;
    }

    /**
     * @return the visual element which has created the element
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }

    /**
     * @return the IOState of this element. The IOState contains the {@link ObservableValue}s of the inputs and outputs.
     */
    public IOState getIoState() {
        return ioState;
    }
}
