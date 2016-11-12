package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * @author hneemann
 */
public class TestProcessor extends TestCase {

    private ToBreakRunner createProcessor(String program) throws IOException, PinException, NodeException, ElementNotFoundException {
        ToBreakRunner runner = new ToBreakRunner("../../main/dig/processor/Processor.dig", false);
        Model model = runner.getModel();

        ROM rom = null;
        for (ROM r : model.findNode(ROM.class)) {
            if (r.isProgramMemory())
                rom = r;
        }
        assertNotNull(rom);

        rom.setData(new DataField(new File(Resources.getRoot(), program)));

        runner.getModel().init(true);
        return runner;
    }

    /**
     * Loads the simulated processor, and loads a program that calculates the 15th
     * fibonacci number with a simple recursive algorithm. The result (610) is stored in the first RAM word.
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testFibonacci() throws IOException, NodeException, PinException, ElementNotFoundException {
        RAMSinglePort ram =
                createProcessor("programs/fibonacci.hex")
                        .runToBreak(98644)
                        .getSingleNode(RAMSinglePort.class);

        assertEquals(610, ram.getMemory().getDataWord(0));
    }

    /**
     * Loads the simulated processor, and loads a processors self test.
     * If a 2 is written to memory address 0x100 test was passed!
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testProcessorSelfTest() throws IOException, NodeException, PinException, ElementNotFoundException {
        RAMSinglePort ram =
                createProcessor("programs/selftest.hex")
                        .runToBreak(700)
                        .getSingleNode(RAMSinglePort.class);

        assertEquals(2, ram.getMemory().getDataWord(256));
    }


}
