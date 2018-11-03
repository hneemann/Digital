/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.lang.Lang;

/**
 * A simple double throw switch
 */
public final class PlainSwitchDT {
    private final PlainSwitch s1;
    private final PlainSwitch s2;
    private final ObservableValue outputAB;
    private final ObservableValue outputAC;
    private final ObservableValue outputB;
    private final ObservableValue outputC;


    PlainSwitchDT(int bits, int num) {
        outputAB = new ObservableValue("A" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin"));
        outputAC = new ObservableValue("AC" + num, bits).setBidirectional().setToHighZ().setDescription(PinDescription.IGNORE);
        outputB = new ObservableValue("B" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin"));
        outputC = new ObservableValue("C" + num, bits).setBidirectional().setToHighZ().setDescription(Lang.get("elem_Switch_pin"));
        s1 = new PlainSwitch(outputAB, outputB, false);
        s2 = new PlainSwitch(outputAC, outputC, true);
    }

    /**
     * Adds the outputs to the given builder
     *
     * @param ov the builder to use
     */
    public void addOutputs(ObservableValues.Builder ov) {
        ov.add(outputAB, outputAC, outputB, outputC);
    }

    /**
     * Sets the inputs of this switch
     *
     * @param inAB first input of pin A
     * @param inAC second input of pin A
     * @param inB  pin B
     * @param inC  Pin C
     * @throws NodeException NodeException
     */
    public void setInputs(ObservableValue inAB, ObservableValue inAC, ObservableValue inB, ObservableValue inC) throws NodeException {
        s1.setInputs(inAB, inB);
        s2.setInputs(inAC, inC);
    }

    /**
     * Initializes the switch
     *
     * @param model the model
     */
    public void init(Model model) {
        s1.init(model);
        s2.init(model);
    }

    /**
     * Sets the state of the switch
     *
     * @param isClosed true is A-B is closed and A-C is open
     */
    public void setClosed(boolean isClosed) {
        s1.setClosed(isClosed);
        s2.setClosed(!isClosed);
    }

    /**
     * calles is state has changed
     */
    public void hashChanged() {
        s1.hasChanged();
        s2.hasChanged();
    }

    /**
     * Adds the outputs to the given builder
     *
     * @param ov the builder
     */
    public void addOutputsTo(ObservableValues.Builder ov) {
        ov.add(outputAB, outputAC, outputB, outputC);
    }
}
