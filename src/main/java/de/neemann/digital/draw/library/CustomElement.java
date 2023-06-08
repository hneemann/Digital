/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.model.ModelCreator;

/**
 * This class represents a custom, nested element.
 * So it is possible to use an element in the circuit witch is made from an
 * existing circuit. So you can build hierarchical circuits.
 */
public class CustomElement implements Element {
    private final ElementTypeDescriptionCustom descriptionCustom;

    /**
     * Creates a new custom element
     *
     * @param descriptionCustom the inner circuit
     */
    public CustomElement(ElementTypeDescriptionCustom descriptionCustom) {
        this.descriptionCustom = descriptionCustom;
    }

    /**
     * Gets a {@link ModelCreator} of this circuit.
     * Every time this method is called a new {@link ModelCreator} is created.
     *
     * @param library                 the element library
     * @param subName                 name of the circuit, used to name unique elements
     * @param depth                   recursion depth, used to detect a circuit which contains itself
     * @param errorVisualElement      visual element used for error indicating
     * @param containingVisualElement the containing visual element
     * @return the {@link ModelCreator}
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator getModelCreator(LibraryInterface library, String subName, int depth, VisualElement errorVisualElement, VisualElement containingVisualElement) throws PinException, NodeException, ElementNotFoundException {
        return descriptionCustom.getModelCreator(library, subName, depth, errorVisualElement, containingVisualElement);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new RuntimeException("invalid call!");
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return descriptionCustom.getCircuit().getOutputNames();
    }

    @Override
    public void registerNodes(Model model) {
        throw new RuntimeException("invalid call!");
    }

    /**
     * @return the circuit which is represented by this element
     */
    public Circuit getCircuit() {
        return descriptionCustom.getCircuit();
    }
}
