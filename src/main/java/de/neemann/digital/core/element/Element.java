/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.components.CircuitModifier;

/**
 * A concrete element used for the simulation
 */
public interface Element {
    /**
     * Sets the inputs for this element.
     * This list contains the outputs of other elements which are connected to the
     * inputs of this element.
     *
     * @param inputs the list of <code>ObservableValue</code>s to use
     * @throws NodeException NodeException
     */
    void setInputs(ObservableValues inputs) throws NodeException;

    /**
     * When the connections between the elements are build, all outputs are collected
     * by calling this method. After the interconnection they are set to the inputs
     * by calling <code>setInputs()</code>
     *
     * @return the list of outputs which are set by this element
     * @throws PinException PinException
     */
    ObservableValues getOutputs() throws PinException;

    /**
     * The element has to register its nodes to the model.
     * This method also is used to register special items at the model.
     *
     * @param model the model to register to
     */
    void registerNodes(Model model);

    /**
     * Is called after registerNodes is called on all Elements.
     *
     * @param model the model this element belongs to
     * @throws NodeException NodeException
     */
    default void init(Model model) throws NodeException {
    }

    /**
     * Needs to be implemented if a circuit is to be modified by the running model.
     * The EEPROM is a example which does that.
     *
     * @param visualElement   the visualElement in the circuit
     * @param circuitModifier interface to modify the circuit
     */
    default void enableCircuitModification(VisualElement visualElement, CircuitModifier circuitModifier) {
    }
}
