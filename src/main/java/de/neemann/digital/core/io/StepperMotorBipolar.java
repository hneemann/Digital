/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The bipolar stepper motor
 */
public class StepperMotorBipolar extends StepperMotorUnipolar {

    /**
     * The bipolar motor description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(StepperMotorBipolar.class, input("A+"), input("A-"), input("B+"), input("B-"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERT_OUTPUT);

    private ObservableValue aPlus;
    private ObservableValue aMinus;
    private ObservableValue bPlus;
    private ObservableValue bMinus;

    /**
     * Creates a new instance
     *
     * @param attr the motors attributes
     */
    public StepperMotorBipolar(ElementAttributes attr) {
        super(attr);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        aPlus = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        aMinus = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        bPlus = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        bMinus = inputs.get(3).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    protected boolean reverse() {
        return false;
    }

    @Override
    protected int getState() {
        int a = getCoilState(aPlus, aMinus);
        int b = getCoilState(bPlus, bMinus);

        int state = 0;
        if (a < 0)
            state |= 1;
        if (a > 0)
            state |= 4;
        if (b < 0)
            state |= 2;
        if (b > 0)
            state |= 8;

        return state;
    }

    private int getCoilState(ObservableValue aPlus, ObservableValue aMinus) {
        if (!aPlus.isHighZ() && !aMinus.isHighZ()) {
            if (aPlus.getBool() && !aMinus.getBool())
                return 1;
            if (!aPlus.getBool() && aMinus.getBool())
                return -1;
        }
        return 0;
    }
}
