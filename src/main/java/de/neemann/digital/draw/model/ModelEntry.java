/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The ModelEntry is used to generate the model.
 * It holds the element which is able to register nodes to the model and the visual element which is
 * the visual representation of the circuits.
 */
public class ModelEntry {
    private final Element element;
    private final Pins pins;
    private final PinDescriptions inputNames;
    private final boolean isNestedElement;
    private final File origin;                           // Only used to create better error messages
    private final VisualElement containingVisualElement; // Only used to create better error messages
    private final VisualElement visualElement;
    private IOState ioState;

    /**
     * Creates a new instance
     *
     * @param element                 the element which is created
     * @param pins                    the pins transformed in the circuits coordinate system
     * @param visualElement           the visual element which has created the element
     * @param inputNames              the pin descriptions of the inputs.
     * @param isNestedElement         true if this visual element is a nested included element
     * @param origin                  Used to create better error messages
     * @param containingVisualElement only used to create better error messages
     */
    public ModelEntry(Element element, Pins pins, VisualElement visualElement, PinDescriptions inputNames, boolean isNestedElement, File origin, VisualElement containingVisualElement) {
        this.element = element;
        this.pins = pins;
        this.visualElement = visualElement;
        this.inputNames = inputNames;
        this.isNestedElement = isNestedElement;
        this.origin = origin;
        this.containingVisualElement = containingVisualElement;
    }

    /**
     * Sets the Inputs of the element contained in this entry
     *
     * @throws PinException  PinException
     * @throws NodeException NodeException
     */
    public void applyInputs() throws PinException, NodeException {
        try {
            HashMap<String, Pin> ins = pins.getInputs();

            InverterConfig ic = visualElement.getElementAttributes().get(Keys.INVERTER_CONFIG);

            ObservableValues values = ObservableValues.EMPTY_LIST;
            ArrayList<ObservableValue> inputs = new ArrayList<>();
            for (PinDescription inputName : inputNames) {
                Pin pin = ins.get(inputName.getName());
                if (pin == null)
                    throw new PinException(Lang.get("err_pin_N0_atElement_N1_notFound", inputName, visualElement), containingVisualElement);

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException(Lang.get("err_noValueSetFor_N0_atElement_N1", inputName, visualElement), containingVisualElement);

                inputs.add(ic.invert(inputName.getName(), value));
            }

            ArrayList<ObservableValue> bidirect = null;
            for (Pin p : pins) {
                if (p.getDirection() == Pin.Direction.both) {
                    if (bidirect == null)
                        bidirect = new ArrayList<>();
                    final ObservableValue readerValue = p.getReaderValue();
                    if (readerValue == null && !p.isSwitchPin())
                        throw new PinException(Lang.get("err_noValueSetFor_N0_atElement_N1", p.getName(), visualElement), containingVisualElement);
                    bidirect.add(readerValue);
                }
            }
            if (bidirect != null)
                inputs.addAll(bidirect);

            if (inputs.size() > 0) {
                values = new ObservableValues(inputs);
                element.setInputs(values);
            }
            ioState = new IOState(values, element.getOutputs(), element);
        } catch (PinException | NodeException e) {
            e.setOrigin(origin);
            e.setVisualElement(containingVisualElement);
            throw e;
        }
    }

    /**
     * Connects this model to the gui.
     */
    public void connectToGui() {
        if (!isNestedElement) {
            if (ioState == null)
                throw new RuntimeException("call applyInputs before connectToGui");
            visualElement.setState(ioState);
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

    /**
     * @return the containing visual element
     */
    public VisualElement getContainingVisualElement() {
        return containingVisualElement;
    }
}
