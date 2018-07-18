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

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple relay.
 */
public class Relay extends Node implements Element {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Relay.class, input("in1"), input("in2"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.POLES)
            .addAttribute(Keys.RELAY_NORMALLY_CLOSED);

    private final Pole[] poles;
    private final boolean invers;
    private ObservableValue input1;
    private ObservableValue input2;
    private boolean isClosed;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Relay(ElementAttributes attr) {
        this.invers = attr.get(Keys.RELAY_NORMALLY_CLOSED);
        int bits = attr.getBits();
        int poleCount = attr.get(Keys.POLES);
        poles = new Pole[poleCount];
        for (int i = 0; i < poleCount; i++)
            poles[i] = new Pole(bits, i + 1);
    }

    @Override
    public ObservableValues getOutputs() {
        ObservableValues.Builder ov = new ObservableValues.Builder();
        for (Pole p : poles)
            p.addOutputs(ov);
        return ov.build();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input1 = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        input2 = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        int i = 2;
        for (Pole p : poles) {
            p.setInputs(inputs.get(i), inputs.get(i + 1));
            i += 2;
        }
    }

    @Override
    public void readInputs() {
        if (input1.isHighZ() || input2.isHighZ())
            isClosed = invers;
        else
            isClosed = (input1.getBool() ^ input2.getBool()) ^ invers;
    }

    @Override
    public void writeOutputs() {
        for (Pole p : poles)
            p.setClosed(isClosed);
    }

    @Override
    public void init(Model model) {
        for (Pole p : poles)
            p.init(model);
    }

    /**
     * @return true if closed
     */
    public boolean isClosed() {
        return isClosed;
    }

    private static final class Pole {
        private final Switch s;
        private final ObservableValue outputA;
        private final ObservableValue outputB;

        private Pole(int bits, int num) {
            outputA = new ObservableValue("A" + num, bits).setBidirectional().setToHighZ();
            outputB = new ObservableValue("B" + num, bits).setBidirectional().setToHighZ();
            s = new Switch(outputA, outputB, false);
        }

        private void addOutputs(ObservableValues.Builder ov) {
            ov.add(outputA, outputB);
        }

        public void setInputs(ObservableValue inA, ObservableValue inB) throws NodeException {
            s.setInputs(new ObservableValues(inA, inB));
        }

        public void init(Model model) {
            s.init(model);
        }

        public void setClosed(boolean isClosed) {
            s.setClosed(isClosed);
        }
    }
}
