/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.lang.Lang;

/**
 * The Reset element
 */
public class Reset implements Element {

    /**
     * The Reset description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Reset", Reset.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERT_OUTPUT)
            .supportsHDL();

    private final ObservableValue output;
    private final boolean invOut;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Reset(ElementAttributes attributes) {
        output = new ObservableValue("Reset", 1).setPinDescription(DESCRIPTION);
        invOut = attributes.get(Keys.INVERT_OUTPUT);
        output.setBool(!invOut);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        model.addReset(this);
    }

    /**
     * Clears the reset state.
     */
    public void clearReset() {
        output.setBool(invOut);
    }

}
