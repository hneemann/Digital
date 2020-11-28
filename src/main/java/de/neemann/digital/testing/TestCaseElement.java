/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.*;

/**
 * Dummy to represent the testdata in the circuit.
 */
public class TestCaseElement implements Element {

    /**
     * The TestCaseElement description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("Testcase", TestCaseElement.class)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.TESTDATA)
            .addAttribute(Keys.ENABLED)
            .supportsHDL();

    /**
     * creates a new instance
     *
     * @param attributes the attributes
     */
    public TestCaseElement(ElementAttributes attributes) {
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
