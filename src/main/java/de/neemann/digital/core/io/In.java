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
import de.neemann.digital.lang.Lang;

/**
 * The Input
 */
public class In implements Element {

    /**
     * The inputs description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(In.class) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            String d = Lang.evalMultilingualContent(elementAttributes.get(Keys.DESCRIPTION));
            if (d.length() > 0)
                return d;
            else
                return super.getDescription(elementAttributes);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INPUT_DEFAULT)
            .addAttribute(Keys.IS_HIGH_Z)
            .addAttribute(Keys.AVOID_ACTIVE_LOW)
            .addAttribute(Keys.DESCRIPTION)
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.PINNUMBER)
            .addAttribute(Keys.ADD_VALUE_TO_GRAPH)
            .supportsHDL();

    private final ObservableValue output;
    private final String label;
    private final String pinNumber;
    private final IntFormat format;
    private final boolean showInGraph;
    private Model model;
    private ObservableValue input;

    /**
     * Create a new instance
     *
     * @param attributes the inputs attributes
     */
    public In(ElementAttributes attributes) {
        InValue value = attributes.get(Keys.INPUT_DEFAULT);
        pinNumber = attributes.get(Keys.PINNUMBER);
        output = new ObservableValue("out", attributes.get(Keys.BITS))
                .setPinDescription(DESCRIPTION)
                .setPinNumber(pinNumber);

        if (attributes.get(Keys.IS_HIGH_Z) || value.isHighZ())
            output.setBidirectional();

        if (value.isHighZ())
            output.setToHighZ();
        else
            output.setValue(value.getValue());

        label = attributes.getLabel();
        format = attributes.get(Keys.INT_FORMAT);
        showInGraph = attributes.get(Keys.ADD_VALUE_TO_GRAPH);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        // if input is bidirectional the value is given to read the pins state!
        input = inputs.get(0);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        model.addInput(new Signal(label, output, output::set)
                .setPinNumber(pinNumber)
                .setBidirectionalReader(input)
                .setShowInGraph(showInGraph)
                .setFormat(format));
        this.model = model;
    }

    /**
     * @return the model this input is attached to
     */
    public Model getModel() {
        return model;
    }
}
