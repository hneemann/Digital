/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.element.PinInfo;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Puts the pins name and the pins x-y-position together!
 */
public class Pin extends PinInfo {

    private final Vector pos;
    private ObservableValue value;
    private ObservableValue readerValue;  // reader for bidirectional pins
    private VisualElement visualElement;  // only used to create better error messages!


    /**
     * Creates a new pin
     *
     * @param pos the position
     * @param pin the PinDescription
     */
    public Pin(Vector pos, PinDescription pin) {
        super(pin);
        this.pos = pos;
    }

    /**
     * @return the pins position
     */
    public Vector getPos() {
        return pos;
    }

    /**
     * @return the value which represents the pin state
     */
    public ObservableValue getValue() {
        return value;
    }

    /**
     * Sets the value which represents the pins state
     *
     * @param value the ObservableValue
     */
    public void setValue(ObservableValue value) {
        this.value = value;
    }

    /**
     * If the pin is bidirectional there are two values, one which can be used to read the pins state
     * and one to write the pins state.
     *
     * @return returns the bidirectional reader
     */
    public ObservableValue getReaderValue() {
        return readerValue;
    }

    /**
     * Sets the bidirectional reader.
     *
     * @param readerValue the bidirectional reader
     * @see Pin#getReaderValue()
     */
    public void setReaderValue(ObservableValue readerValue) {
        this.readerValue = readerValue;
    }

    /**
     * Sets the visual element this pin belongs to
     *
     * @param visualElement the element this pin belongs to
     * @return this for chained calls
     */
    public Pin setVisualElement(VisualElement visualElement) {
        this.visualElement = visualElement;
        return this;
    }

    @Override
    public String toString() {
        if (visualElement!=null)
            return super.toString()+"; "+visualElement;
        else
            return super.toString();
    }
}
