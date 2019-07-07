/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.lang.Lang;

/**
 * A simple double throw switch
 */
public final class PlainSwitchDT implements NodeInterface {
    private final ObservableValue outputA;
    private final ObservableValue outputB;
    private final ObservableValue outputC;
    private final int bits;
    private PlainSwitch.SwitchModel s1;
    private PlainSwitch.SwitchModel s2;
    private boolean closed = false;


    PlainSwitchDT(int bits, int num) {
        this.bits = bits;
        outputA = new ObservableValue("A" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin")).setSwitchPin(true);
        outputB = new ObservableValue("B" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin")).setSwitchPin(true);
        outputC = new ObservableValue("C" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin")).setSwitchPin(true);
    }

    /**
     * Sets the inputs of this switch
     *
     * @param inA first input, the DT switch anchor
     * @param inB pin B
     * @param inC Pin C
     * @throws NodeException NodeException
     */
    public void setInputs(ObservableValue inA, ObservableValue inB, ObservableValue inC) throws NodeException {
        if (inA != null) {
            if (inB != null && inC == null) {
                inA.addObserverToValue(this).checkBits(bits, null);
                inB.addObserverToValue(this).checkBits(bits, null);
                s1 = PlainSwitch.createSwitchModel(inA, inB, outputA, outputB);
            } else if (inB == null && inC != null) {
                inA.addObserverToValue(this).checkBits(bits, null);
                inC.addObserverToValue(this).checkBits(bits, null);
                s2 = PlainSwitch.createSwitchModel(inA, inC, outputA, outputC);
            } else if (inB != null && inC != null) {
                inA.addObserverToValue(this).checkBits(bits, null);
                inB.addObserverToValue(this).checkBits(bits, null);
                inC.addObserverToValue(this).checkBits(bits, null);
                s1 = PlainSwitch.createSwitchModel(inA, inB, outputA, outputB);
                s2 = PlainSwitch.createSwitchModel(inA, inC, outputA, outputC);
                if (s1.isUniDirOutput(outputA) && s2.isUniDirOutput(outputA)) {
                    s1.dontTouchOpenContact();
                    s2.dontTouchOpenContact();
                }
            }
        }
    }

    /**
     * Initializes the switch
     *
     * @param model the model
     */
    public void init(Model model) {
        if (s1 != null) {
            s1.setModel(model);
            s1.setClosed(closed);
        }
        if (s2 != null) {
            s2.setModel(model);
            s2.setClosed(!closed);
        }
        hasChanged();
    }

    /**
     * Sets the state of the switch
     *
     * @param isClosed true means A-B is closed and A-C is open
     */
    public void setClosed(boolean isClosed) {
        if (this.closed != isClosed) {
            this.closed = isClosed;
            if (s1 != null)
                s1.setClosed(closed);
            if (s2 != null)
                s2.setClosed(!closed);
            hasChanged();
        }
    }

    @Override
    public void hasChanged() {
        if (s1 != null)
            s1.propagate();
        if (s2 != null)
            s2.propagate();
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(outputA, outputB, outputC);
    }

    /**
     * Adds the outputs to the given builder
     *
     * @param ov the builder
     */
    void addOutputsTo(ObservableValues.Builder ov) {
        ov.add(outputA, outputB, outputC);
    }

    /**
     * @return the number of bits switched
     */
    public int getBits() {
        return bits;
    }
}
