/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 */
public class TestProcessor extends TestCase {

    private ToBreakRunner createProcessor(String program) throws IOException, PinException, NodeException, ElementNotFoundException {
        return createProcessor(program, "../../main/dig/processor/Processor.dig");
    }

    private ToBreakRunner createProcessor(String program, String processor) throws IOException, PinException, NodeException, ElementNotFoundException {
        ToBreakRunner runner = new ToBreakRunner(processor, false);
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
     * Loads the simulated processor, and loads a program that calculates the 15th
     * fibonacci number with a simple recursive algorithm. The result (610) is stored in the first RAM word.
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testFibonacciMux() throws IOException, NodeException, PinException, ElementNotFoundException {
        ToBreakRunner processor = createProcessor("programs/fibonacci.hex", "../../main/dig/processor/ProcessorMux.dig");
        processor.getModel().getInput("reset").setBool(false);
        RAMDualPort ram = processor
                .runToBreak(98644)
                .getSingleNode(RAMDualPort.class);

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

    /**
     * Loads the simulated processor, and loads a processors self test.
     * If a 2 is written to memory address 0x100 test was passed!
     *
     * @throws IOException   IOException
     * @throws NodeException NodeException
     * @throws PinException  PinException
     */
    public void testProcessorSelfTestMux() throws IOException, NodeException, PinException, ElementNotFoundException {
        ToBreakRunner processor = createProcessor("programs/selftest.hex", "../../main/dig/processor/ProcessorMux.dig");
        processor.getModel().getInput("reset").setBool(false);
        RAMDualPort ram = processor
                .runToBreak(700)
                .getSingleNode(RAMDualPort.class);

        assertEquals(2, ram.getMemory().getDataWord(256));
    }


}
