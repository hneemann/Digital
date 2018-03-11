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
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.lang.Lang;

/**
 * This class represents a custom, nested element.
 * So it is possible to use an element in the circuit witch is made from an
 * existing circuit. So you can build hierarchical circuits.
 */
public class CustomElement implements Element {
    private static final int MAX_DEPTH = 30;

    private final Circuit circuit;
    private final ElementLibrary library;
    private NetList netList;

    /**
     * Creates a new custom element
     *
     * @param circuit the inner circuit
     * @param library the library to use.
     */
    public CustomElement(Circuit circuit, ElementLibrary library) {
        this.circuit = circuit;
        this.library = library;
    }

    /**
     * Gets a {@link ModelCreator} of this circuit.
     * Every time this method is called a new {@link ModelCreator} is created.
     *
     * @param subName                 name of the circuit, used to name unique elements
     * @param depth                   recursion depth, used to detect a circuit which contains itself
     * @param containingVisualElement the containing visual element
     * @return the {@link ModelCreator}
     * @throws PinException             PinException
     * @throws NodeException            NodeException
     * @throws ElementNotFoundException ElementNotFoundException
     */
    public ModelCreator getModelCreator(String subName, int depth, VisualElement containingVisualElement) throws PinException, NodeException, ElementNotFoundException {
        if (netList == null)
            netList = new NetList(circuit);

        if (depth > MAX_DEPTH)
            throw new NodeException(Lang.get("err_recursiveNestingAt_N0", circuit.getOrigin()));

        return new ModelCreator(circuit, library, true, new NetList(netList, containingVisualElement), subName, depth, containingVisualElement);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new RuntimeException("invalid call!");
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return circuit.getOutputNames();
    }

    @Override
    public void registerNodes(Model model) {
        throw new RuntimeException("invalid call!");
    }

    /**
     * @return the circuit which is represented by this element
     */
    public Circuit getCircuit() {
        return circuit;
    }
}
