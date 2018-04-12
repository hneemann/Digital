/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.clock;

import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;

import java.util.ArrayList;

/**
 * Used to utilize the board specific clock resources
 */
public interface HDLClockIntegrator {

    /**
     * Modifies the circuit to integrate the clock sources
     *
     * @param circuit the circuit
     * @param clocks  the clock input ports
     * @throws HDLException HDLException
     */
    void integrateClocks(HDLCircuit circuit, ArrayList<ClockInfo> clocks) throws HDLException;

}

