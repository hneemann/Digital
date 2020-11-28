/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * Base class of all flip-flops storing a single bit
 */
abstract class FlipflopBit extends Node implements Element {

    private final boolean isProbe;
    private final String label;
    private ObservableValue q;
    private ObservableValue qn;
    private boolean out;

    /**
     * Creates a new instance
     *
     * @param attributes  the attributes
     * @param description the description of this flip flop
     */
    FlipflopBit(ElementAttributes attributes, ElementTypeDescription description) {
        super(true);
        this.q = new ObservableValue("Q", 1).setPinDescription(description);
        this.qn = new ObservableValue("~Q", 1).setPinDescription(description);
        isProbe = attributes.get(Keys.VALUE_IS_PROBE);
        label = attributes.getLabel();

        long def = attributes.get(Keys.DEFAULT);
        out = def > 0;
        q.setBool(out);
        qn.setBool(!out);
    }

    /**
     * Creates a new instance
     *
     * @param label the label
     * @param q     the output
     * @param qn    the inverted output
     */
    FlipflopBit(String label, ObservableValue q, ObservableValue qn) {
        super(true);
        this.q = q;
        this.qn = qn;
        isProbe = false;
        this.label = label;
        q.setBool(false);
        qn.setBool(true);
    }

    @Override
    public void writeOutputs() throws NodeException {
        q.setBool(out);
        qn.setBool(!out);
    }

    @Override
    public ObservableValues getOutputs() {
        return ovs(q, qn);
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public void registerNodes(Model model) {
        super.registerNodes(model);
        if (isProbe)
            model.addSignal(new Signal(label, q, (v, z) -> {
                out = v != 0;
                q.setBool(out);
                qn.setBool(!out);
            }).setTestOutput());
    }

    void setOut(boolean out) {
        this.out = out;
    }

    boolean isOut() {
        return out;
    }

}
