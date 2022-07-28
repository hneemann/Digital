/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The Break element
 */
public class Break extends Node implements Element {

    /**
     * The Break description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Break.class, input("brk"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.ENABLED)
            .addAttribute(Keys.CYCLES);

    private final int cycles;
    private final String label;
    private final boolean enabled;
    private ObservableValue input;
    private boolean lastBrk;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Break(ElementAttributes attributes) {
        cycles = attributes.get(Keys.CYCLES);
        enabled = attributes.get(Keys.ENABLED);
        label = attributes.getLabel();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(1, null);
    }

    @Override
    public void readInputs() throws NodeException {
        boolean brk = input.getBool();
        if (enabled && brk && !lastBrk) {
            getModel().breakDetected();
        }
        lastBrk = brk;
    }

    @Override
    public void writeOutputs() throws NodeException {
    }

    /**
     * @return the break value
     */
    public ObservableValue getBreakInput() {
        return input;
    }

    /**
     * @return the timeout cycles
     */
    public int getCycles() {
        return cycles;
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        model.addBreak(this);
    }

    /**
     * @return the break label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
