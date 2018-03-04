/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model;

/**
 * Creates a simple generic clock divider.
 * Inserts a simple clock divider to match the frequency given in the model.
 * Not suited for real world applications because a logical signal is used
 * to clock the entities.
 * You should use the FPGA-dependant clock resources to generate a clock signal.
 */
public class ClockIntegratorGeneric implements ClockIntegrator {
    private double periodns;

    /**
     * Creates a new instance
     *
     * @param periodns the clock period in ns
     */
    public ClockIntegratorGeneric(double periodns) {
        this.periodns = periodns;
    }

    @Override
    public void integrateClocks(HDLModel model) throws HDLException {
        for (HDLClock c : model.getClocks()) {
            int freq = c.getFrequency();
            int counter = (int) (1000000000.0 / (periodns * 2 * freq));

            if (counter >= 2) {
                Port cOut = new Port("out", Port.Direction.out, "Clock").setBits(1);
                Port cIn = new Port("in", Port.Direction.in, "Source").setBits(1);

                Signal oldSig = c.getClockPort().getSignal();
                Signal newSig = model.createSignal();
                oldSig.replaceWith(newSig);
                newSig.addPort(cOut);
                oldSig.addPort(cIn);

                model.addNode(new HDLClockNode(counter, new Ports().add(cIn).add(cOut)));
            }
        }
    }
}
