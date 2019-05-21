/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.clock;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.HDLCircuit;

import java.util.ArrayList;

/**
 * Creates a simple generic clock divider.
 * Inserts a simple clock divider to match the frequency given in the model.
 * Not suited for real world applications because a logical signal is used
 * to clock the entities.
 * You should use the FPGA-dependant clock resources to generate a clock signal.
 */
public class ClockIntegratorGeneric implements HDLClockIntegrator {
    private static final Key<Integer> COUNTER_KEY = new Key<>("maxCounter", 0);
    private double periodns;
    private String clockGenerator;

    /**
     * Creates a new instance
     *
     * @param periodns the clock period in ns
     */
    public ClockIntegratorGeneric(double periodns) {
        this.periodns = periodns;
    }

    /**
     * Enables an external clock generator
     *
     * @param clockGenerator the clock generator
     * @return this for chained calls
     */
    public ClockIntegratorGeneric setClockGenerator(String clockGenerator) {
        this.clockGenerator = clockGenerator;
        return this;
    }

    @Override
    public void integrateClocks(HDLCircuit circuit, ArrayList<ClockInfo> clocks) throws HDLException {
        for (ClockInfo ci : clocks) {
            int freq = ci.getFrequency();
            int counter = (int) (1000000000.0 / (periodns * 2 * freq));

            if (counter >= 2) {
                final ElementAttributes attributes = new ElementAttributes().set(COUNTER_KEY, counter);
                if (clockGenerator != null)
                    attributes.set(new Key<>("clockGenerator", ""), clockGenerator);
                HDLNodeBuildIn node = new HDLNodeBuildIn("simpleClockDivider",
                        attributes,
                        name -> 1);

                circuit.integrateClockNode(ci.getClockPort(), node);
            }
        }
    }

}
