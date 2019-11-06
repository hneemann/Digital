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
 * A simple manually controlled switch
 */
public class Switch implements Element, NodeInterface, Countable {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Switch.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.MIRROR)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.POLES)
            .addAttribute(Keys.CLOSED)
            .addAttribute(Keys.SWITCH_ACTS_AS_INPUT);

    private final PlainSwitch[] poles;
    private final String label;
    private final boolean switchActsAsInput;
    private boolean closed;
    private ObservableValue value;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Switch(ElementAttributes attr) {
        this(attr, attr.get(Keys.CLOSED));
    }

    /**
     * Create a new instance
     *
     * @param attr   the attributes
     * @param closed initial state
     */
    public Switch(ElementAttributes attr, boolean closed) {
        this.closed = closed;
        int bits = attr.getBits();
        int poleCount = attr.get(Keys.POLES);
        poles = new PlainSwitch[poleCount];
        for (int i = 0; i < poleCount; i++)
            poles[i] = new PlainSwitch(bits, closed, "A" + (i + 1), "B" + (i + 1));
        label = attr.getLabel();
        switchActsAsInput = attr.get(Keys.SWITCH_ACTS_AS_INPUT);
    }

    @Override
    public ObservableValues getOutputs() {
        ObservableValues.Builder ov = new ObservableValues.Builder();
        for (PlainSwitch p : poles)
            p.addOutputsTo(ov);
        return ov.build();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        int i = 0;
        for (PlainSwitch p : poles) {
            p.setInputs(inputs.get(i), inputs.get(i + 1));
            i += 2;
        }
    }

    @Override
    public void init(Model model) {
        for (PlainSwitch p : poles)
            p.init(model);
    }

    @Override
    public void registerNodes(Model model) {
        if (switchActsAsInput && !label.isEmpty()) {
            value = new ObservableValue(label, 1);
            value.addObserver(new NodeInterface() {
                @Override
                public ObservableValues getOutputs() {
                    return value.asList();
                }

                @Override
                public void hasChanged() {
                    setClosed(value.getBool());
                }
            });
            model.addInput(new Signal(label, value));
        }
    }

    @Override
    public void hasChanged() {
        for (PlainSwitch p : poles)
            p.hasChanged();
    }

    /**
     * Sets the state of the switch
     *
     * @param closed true if closed
     */
    public void setClosed(boolean closed) {
        if (this.closed != closed) {
            if (value != null)
                value.setBool(closed);
            this.closed = closed;
            for (PlainSwitch p : poles)
                p.setClosed(closed);
        }
    }

    /**
     * @return the state
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
