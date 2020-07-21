/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.memory.Register;
import junit.framework.TestCase;

/**
 */
public class TestRunToBreak extends TestCase {

    public void testRunToBreak() throws Exception {
        new ToBreakRunner("dig/runToBreak.dig")
                .runToBreak(509);
    }

    /**
     * Runs two 5 bit cascaded counters up to an overflow of second counter
     * The 10 bit value is stored in a register.
     */
    public void testCounterSplitter() throws Exception {
        Register r = new ToBreakRunner("dig/CounterSplitter.dig")
                .runToBreak(2045)
                .getSingleNode(Register.class);

        assertEquals(0x3fe, r.getOutputs().get(0).getValue());
    }
}
