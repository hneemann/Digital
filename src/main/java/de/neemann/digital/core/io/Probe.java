/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The measurement Probe
 */
public class Probe implements Element {

    /**
     * The Probe description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("Probe", Probe.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INT_FORMAT);

    private final String label;
    private final IntFormat format;
    private ObservableValue value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Probe(ElementAttributes attributes) {
        label = attributes.get(Keys.LABEL);
        format = attributes.get(Keys.INT_FORMAT);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        value = inputs.get(0);
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
        model.addSignal(new Signal(label, value).setFormat(format));
    }

}
