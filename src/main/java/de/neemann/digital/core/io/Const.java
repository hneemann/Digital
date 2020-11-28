/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

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
 * A constant
 */
public class Const implements Element {

    /**
     * The Constant description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Const.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.VALUE)
            .addAttribute(Keys.INT_FORMAT)
            .supportsHDL();

    private final ObservableValue output;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Const(ElementAttributes attributes) {
        output = new ObservableValue("out", attributes.get(Keys.BITS)).setPinDescription(DESCRIPTION);
        output.setValue(attributes.get(Keys.VALUE));
        output.setConstant();
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
    }
}
