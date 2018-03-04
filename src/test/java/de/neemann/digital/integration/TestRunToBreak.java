/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.Register;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 */
public class TestRunToBreak extends TestCase {

    public void testRunToBreak() throws IOException, NodeException, PinException, ElementNotFoundException {
        new ToBreakRunner("dig/runToBreak.dig")
                .runToBreak(509);
    }

    /**
     * Runs two 5 bit cascaded counters up to an overflow of second counter
     * The 10 bit value is stored in a register.
     *
     * @throws IOException
     * @throws NodeException
     * @throws PinException
     */
    public void testCounterSplitter() throws IOException, NodeException, PinException, ElementNotFoundException {
        Register r = new ToBreakRunner("dig/CounterSplitter.dig")
                .runToBreak(2045)
                .getSingleNode(Register.class);

        assertEquals(0x3fe, r.getOutputs().get(0).getValue());
    }
}
