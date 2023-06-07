/*
 * Copyright (c) 2023 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.fsm;

import de.neemann.digital.core.Bits;

import java.util.HashMap;

/**
 * Information required to determine the current state and transition based on the value transferred by the
 * running circuit.
 */
public class CircuitRepresentation {
    private int stateMask;
    private HashMap<Integer, Transition> transMap;
    private HashMap<Integer, State> stateMap;

    /**
     * Sets the number of state var bits
     *
     * @param stateVarBits the number of bits
     */
    public void setStateVarBits(int stateVarBits) {
        this.stateMask = (int) Bits.mask(stateVarBits);
        transMap = new HashMap<>();
        stateMap = new HashMap<>();
    }

    /**
     * Adds a state
     *
     * @param number the states number
     * @param state  the state
     */
    public void addState(int number, State state) {
        stateMap.put(number, state);
    }

    /**
     * Adds a transition
     *
     * @param val the transitions number
     * @param t   the transition
     */
    public void addTransition(int val, Transition t) {
        transMap.put(val, t);
    }

    /**
     * Returns the active state based on the value obtained from the running circuit
     *
     * @param activeStateTransition the value obtained from the running circuit
     * @return the current state
     */
    public State getActiveState(int activeStateTransition) {
        if (activeStateTransition >= 0)
            return stateMap.get(activeStateTransition & stateMask);
        else
            return null;
    }

    /**
     * Returns the active transition based on the value obtained from the running circuit
     *
     * @param activeStateTransition the value obtained from the running circuit
     * @return the current transition
     */
    public Transition getActiveTransition(int activeStateTransition) {
        return transMap.get(activeStateTransition);
    }
}
