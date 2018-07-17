/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple relay.
 */
public class RelayDT extends Node implements Element {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(RelayDT.class, input("in1"), input("in2"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.POLES);


    private final Pole[] poles;
    private ObservableValue input1;
    private ObservableValue input2;
    private boolean isClosed;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public RelayDT(ElementAttributes attr) {
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
            p.setInputs(inputs.get(i), inputs.get(i + 1), inputs.get(i + 2), inputs.get(i + 3));
            i += 4;
        }
    }

    @Override
    public void readInputs() {
        if (input1.isHighZ() || input2.isHighZ())
            isClosed = false;
        else
            isClosed = input1.getBool() ^ input2.getBool();
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
        private final Switch s1;
        private final Switch s2;
        private final ObservableValue outputAB;
        private final ObservableValue outputAC;
        private final ObservableValue outputB;
        private final ObservableValue outputC;


        private Pole(int bits, int num) {
            outputAB = new ObservableValue("A" + num, bits).setBidirectional().setToHighZ();
            outputAC = new ObservableValue("AC" + num, bits).setBidirectional().setToHighZ().setDescription(PinDescription.IGNORE);
            outputB = new ObservableValue("B" + num, bits).setBidirectional().setToHighZ();
            outputC = new ObservableValue("C" + num, bits).setBidirectional().setToHighZ();
            s1 = new Switch(outputAB, outputB, false);
            s2 = new Switch(outputAC, outputC, true);
        }

        private void addOutputs(ObservableValues.Builder ov) {
            ov.add(outputAB, outputAC, outputB, outputC);
        }

        public void setInputs(ObservableValue inAB, ObservableValue inAC, ObservableValue inB, ObservableValue inC) throws NodeException {
            s1.setInputs(new ObservableValues(inAB, inB));
            s2.setInputs(new ObservableValues(inAC, inC));
        }

        public void init(Model model) {
            s1.init(model);
            s2.init(model);
        }

        public void setClosed(boolean isClosed) {
            s1.setClosed(isClosed);
            s2.setClosed(!isClosed);
        }
    }

}
