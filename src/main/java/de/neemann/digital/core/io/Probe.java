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
import de.neemann.digital.core.ValueFormatter;

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
            .addAttribute(Keys.INT_FORMAT)
            .addAttribute(Keys.PROBE_MODE)
            .addAttribute(Keys.ADD_VALUE_TO_GRAPH);

    private final String label;
    private final ValueFormatter formatter;
    private final boolean showInGraph;
    private final ProbeMode mode;
    private ObservableValue value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Probe(ElementAttributes attributes) {
        label = attributes.get(Keys.LABEL);
        formatter = attributes.getValueFormatter();
        showInGraph = attributes.get(Keys.ADD_VALUE_TO_GRAPH);
        mode = attributes.get(Keys.PROBE_MODE);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        if (mode == ProbeMode.VALUE)
            value = inputs.get(0);
        else
            value = new Counter(label, inputs.get(0).checkBits(1, null, 0), mode).value;
    }

    /**
     * @return the value to show
     */
    public ObservableValue getValue() {
        return value;
    }

    @Override
    public ObservableValues getOutputs() {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
        model.addSignal(new Signal(label, value).setShowInGraph(showInGraph).setFormat(formatter).setTestOutput());
        model.registerGlobalValue(label, value);
    }

    private static final class Counter implements Observer {
        private final ObservableValue in;
        private final ObservableValue value;
        private final ProbeMode mode;
        private boolean last;
        private long counter;

        private Counter(String label, ObservableValue value, ProbeMode mode) {
            this.in = value;
            this.last = in.getBool();
            this.value = new ObservableValue(label, 64);
            value.addObserver(this);
            this.mode = mode;
        }

        @Override
        public void hasChanged() {
            boolean now = in.getBool();
            boolean inc = false;
            switch (mode) {
                case UP:
                    inc = !last & now;
                    break;
                case DOWN:
                    inc = last & !now;
                    break;
                case BOTH:
                    inc = last != now;
                    break;
            }
            last = now;
            if (inc)
                value.setValue(++counter);
        }

    }
}
