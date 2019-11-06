/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.stats.Countable;

/**
 * A simple double throw switch
 */
public class SwitchDT implements Element, NodeInterface, Countable {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(SwitchDT.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.POLES);

    private final PlainSwitchDT[] poles;
    private boolean closed;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public SwitchDT(ElementAttributes attr) {
        int bits = attr.getBits();
        int poleCount = attr.get(Keys.POLES);
        poles = new PlainSwitchDT[poleCount];
        for (int i = 0; i < poleCount; i++)
            poles[i] = new PlainSwitchDT(bits, i + 1);
    }

    @Override
    public ObservableValues getOutputs() {
        ObservableValues.Builder ov = new ObservableValues.Builder();
        for (PlainSwitchDT p : poles)
            p.addOutputsTo(ov);
        return ov.build();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        int i = 0;
        for (PlainSwitchDT p : poles) {
            p.setInputs(inputs.get(i), inputs.get(i + 1), inputs.get(i + 2));
            i += 3;
        }
    }

    @Override
    public void init(Model model) {
        for (PlainSwitchDT p : poles)
            p.init(model);
    }

    @Override
    public void hasChanged() {
        for (PlainSwitchDT p : poles)
            p.hasChanged();
    }

    @Override
    public void registerNodes(Model model) {
    }

    /**
     * Sets the state of the switch
     *
     * @param closed true if closed
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
        for (PlainSwitchDT p : poles)
            p.setClosed(closed);
    }

    /**
     * @return the state of the switch
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public int getDataBits() {
        return poles[0].getBits();
    }

    @Override
    public int getInputsCount() {
        return poles.length;
    }
}
