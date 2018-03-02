/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * A rotary encoder
 */
public class RotEncoder implements Element {

    /**
     * The rotary encoder description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RotEncoder.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    private final ObservableValue outA;
    private final ObservableValue outB;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public RotEncoder(ElementAttributes attributes) {
        outA= new ObservableValue("A", 1).setPinDescription(DESCRIPTION);
        outB= new ObservableValue("B", 1).setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(outA, outB);
    }

    @Override
    public void registerNodes(Model model) {
    }

}
