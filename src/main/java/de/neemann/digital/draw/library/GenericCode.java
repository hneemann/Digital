/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

/**
 * Allows to a generic init code to a circuit.
 */
public class GenericCode implements Element {
    /**
     * The GenericInitCodeElement description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(GenericCode.class)
            .addAttribute(Keys.GENERICLARGE)
            .supportsHDL();

    /**
     * creates a new instance
     *
     * @param attributes the attributes
     */
    public GenericCode(ElementAttributes attributes) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
    }
}
