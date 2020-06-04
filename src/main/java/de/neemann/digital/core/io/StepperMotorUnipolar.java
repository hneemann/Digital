/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The unipolar stepper motor
 */
public class StepperMotorUnipolar extends Node implements Element {
    /**
     * Steps for a full revolution
     */
    public static final int STEPS = 72;
    private static final int SWITCH_SIZE = 2;
    private static final boolean[] STATE_VALID = new boolean[]{
            false, true, true, true,
            true, false, true, false,
            true, true, false, false,
            true, false, false, false,
    };
    private static final int[][] STEP_TABLE = new int[][]{
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 2, 1, 0, 0, 0, 0, -2, -1, 0, 0, 0, 0, 0, 0},
            {0, -2, 0, -1, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, -1, 1, 0, 0, 0, 2, 0, 0, -2, 0, 0, 0, 0, 0, 0},
            {0, 0, -2, 0, 0, 0, -1, 0, 2, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, -1, -2, 1, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 2, 0, 0, -2, 0, 0, 0, 0, 1, 0, 0, -1, 0, 0, 0},
            {0, 1, 0, 2, 0, 0, 0, 0, -1, 0, 0, 0, -2, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, -1, 0, -2, 0, 1, 2, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    /**
     * The stepper motors description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(StepperMotorUnipolar.class, input("P0"), input("P1"), input("P2"), input("P3"), input("com"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.INVERT_OUTPUT);

    private final ObservableValue s0;
    private final ObservableValue s1;
    private final boolean invertOut;
    private ObservableValue p0;
    private ObservableValue p1;
    private ObservableValue p2;
    private ObservableValue p3;
    private ObservableValue com;
    private int lastState;
    private int pos;
    private int sequenceError;
    private boolean stateError;

    /**
     * Creates a new instance
     *
     * @param attr the motors attributes
     */
    public StepperMotorUnipolar(ElementAttributes attr) {
        s0 = new ObservableValue("S0", 1).setPinDescription(DESCRIPTION);
        s1 = new ObservableValue("S1", 1).setPinDescription(DESCRIPTION);
        invertOut = attr.get(Keys.INVERT_OUTPUT);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        p0 = inputs.get(0).checkBits(1, this).addObserverToValue(this);
        p1 = inputs.get(1).checkBits(1, this).addObserverToValue(this);
        p2 = inputs.get(2).checkBits(1, this).addObserverToValue(this);
        p3 = inputs.get(3).checkBits(1, this).addObserverToValue(this);
        com = inputs.get(4).checkBits(1, this).addObserverToValue(this);
    }

    @Override
    public void readInputs() throws NodeException {
        int state = getState();

        int step = STEP_TABLE[lastState][state];

        if (reverse())
            this.pos -= step;
        else
            this.pos += step;

        if (this.pos < 0) this.pos += STEPS;
        else if (this.pos >= STEPS) this.pos -= STEPS;

        boolean stateValid = STATE_VALID[state];
        if (step == 0 && STATE_VALID[lastState] && stateValid) {
            sequenceError += 8;
            if (sequenceError > 8)
                sequenceError = 8;
        } else if (sequenceError > 0)
            sequenceError--;

        stateError = state != 0 && !stateValid;

        if (stateValid)
            lastState = state;
    }

    /**
     * @return true reverses the direction
     */
    protected boolean reverse() {
        return com.getBool();
    }

    /**
     * Calculates the state of the motor inputs
     *
     * @return the state as a number from 0 to 15
     */
    protected int getState() {
        if (com.isHighZ())
            return 0;

        boolean comIn = com.getBool();
        return (isCurrent(p0, comIn) ? 1 : 0)
                | (isCurrent(p1, comIn) ? 2 : 0)
                | (isCurrent(p2, comIn) ? 4 : 0)
                | (isCurrent(p3, comIn) ? 8 : 0);
    }

    private boolean isCurrent(ObservableValue phase, boolean comIn) {
        if (phase.isHighZ())
            return false;
        return phase.getBool() != comIn;
    }

    @Override
    public void writeOutputs() throws NodeException {
        s0.setBool(invertOut ^ (pos < SWITCH_SIZE || pos > STEPS - SWITCH_SIZE));
        s1.setBool(invertOut ^ (pos > STEPS / 2 - SWITCH_SIZE && pos < STEPS / 2 + SWITCH_SIZE));
    }

    /**
     * Returns the motor position
     *
     * @return the position
     */
    public int getPos() {
        return pos;
    }

    /**
     * @return true if the was an error in the phase pattern
     */
    public boolean isError() {
        return sequenceError > 0 || stateError;
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(s0, s1);
    }
}
