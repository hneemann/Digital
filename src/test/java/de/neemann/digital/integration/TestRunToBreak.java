package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.Register;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * @author hneemann
 */
public class TestRunToBreak extends TestCase {

    public void testRunToBreak() throws IOException, NodeException, PinException {
        new ToBreakRunner("dig/runToBreak.dig")
                .runToBreak(511);
    }

    /**
     * Runs two 5 bit cascaded counters up to a overflow of second counter
     * The 10 bit value is stored in a register.
     *
     * @throws IOException
     * @throws NodeException
     * @throws PinException
     */
    public void testCounterSplitter() throws IOException, NodeException, PinException {
        Register r = new ToBreakRunner("dig/CounterSplitter.dig")
                .runToBreak(2047)
                .getSingleNode(Register.class);

        assertEquals(0x3ff, r.getOutputs().get(0).getValue());
    }

    /**
     * Loads a simulated processor, which has already stored a machine program that calculates the 15th
     * fibonacci number with a simple recursive algorithm. The result (610) is stored in the first RAM word.
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testFibonacci() throws IOException, NodeException, PinException {
        RAMSinglePort ram = new ToBreakRunner("dig/processor/Processor_fibonacci.dig")
                .runToBreak(98644)
                .getSingleNode(RAMSinglePort.class);

        assertEquals(610, ram.getMemory().getDataWord(0));
    }

}
