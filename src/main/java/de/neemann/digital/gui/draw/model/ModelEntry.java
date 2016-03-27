package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.gui.draw.elements.*;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hneemann
 */
public class ModelEntry {
    private final Element element;
    private final Pins pins;
    private final String[] inputNames;
    private final VisualElement visualElement;
    private IOState ioState;

    public ModelEntry(Element element, Pins pins, VisualElement visualElement, String[] inputNames) {
        this.element = element;
        this.pins = pins;
        this.visualElement = visualElement;
        this.inputNames = inputNames;
    }

    /**
     * Sets the Inputs of the element contained in this entry
     *
     * @throws PinException
     * @throws NodeException
     */
    public void applyInputs() throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        ObservableValue[] inputs = new ObservableValue[inputNames.length];
        if (inputNames.length > 0) {
            for (int i = 0; i < inputNames.length; i++) {
                Pin pin = ins.get(inputNames[i]);
                if (pin == null)
                    throw new PinException(Lang.get("err_pin_N0_atElement_N1_notFound", inputNames[i], visualElement), visualElement);

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException(Lang.get("err_noValueSetFor_N0_atElement_N1", inputNames[i], visualElement), visualElement);

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
                inputs = Splitter.combine(inputs, bidirect.toArray(new ObservableValue[bidirect.size()]));

            element.setInputs(inputs);
        }
        ioState = new IOState(inputs, element.getOutputs());
    }

    public void connectToGui(Observer guiObserver) {
        if (ioState == null)
            throw new RuntimeException("call applyInputs before connectToGui");
        visualElement.setState(ioState, guiObserver);
    }

    public Pins getPins() {
        return pins;
    }

    public Element getElement() {
        return element;
    }

    public VisualElement getVisualElement() {
        return visualElement;
    }

    public boolean containsValue(ObservableValue v) {
        return pins.containsValue(v);
    }

    public IOState getIoState() {
        return ioState;
    }
}
