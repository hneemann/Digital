/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 */
public class TestProcessor extends TestCase {

    private ToBreakRunner createProcessor(String program) throws Exception {
        ToBreakRunner runner = new ToBreakRunner("../../main/dig/processor/Processor.dig", false);
        Model model = runner.getModel();

        ROM rom = null;
        for (ROM r : model.findNode(ROM.class)) {
            if (r.isProgramMemory())
                rom = r;
        }
        assertNotNull(rom);

        rom.setData(Importer.read(new File(Resources.getRoot(), program), rom.getDataBits(), false));

        runner.getModel().init(true);
        return runner;
    }

    private ToBreakRunner createProcessorMux(String program) throws Exception {
        ToBreakRunner runner = new ToBreakRunner("../../main/dig/processor/ProcessorHDL.dig", false);
        Model model = runner.getModel();

        ObservableValue instr = model.getInput("Instr");
        ObservableValue pc = model.getOutput("PC");
        assertNotNull(instr);
        assertNotNull(pc);

        DataField data = Importer.read(new File(Resources.getRoot(), program), 16, false);
        pc.addObserverToValue(() -> instr.setValue(data.getDataWord((int) pc.getValue()))).fireHasChanged();

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
    public void testFibonacci() throws Exception {
        RAMSinglePort ram =
                createProcessor("programs/fibonacci.hex")
                        .runToBreak(100616)
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
    public void testFibonacciMux() throws Exception {
        ToBreakRunner processor = createProcessorMux("programs/fibonacci.hex");
        processor.getModel().getInput("reset").setBool(false);
        RAMDualPort ram = processor
                .runToBreak(100616)
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
    public void testProcessorSelfTest() throws Exception {
        RAMSinglePort ram =
                createProcessor("programs/selftest.hex")
                        .runToBreak(790)
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
    public void testProcessorSelfTestMux() throws Exception {
        ToBreakRunner processor = createProcessorMux("programs/selftest.hex");
        processor.getModel().getInput("reset").setBool(false);
        RAMDualPort ram = processor
                .runToBreak(790)
                .getSingleNode(RAMDualPort.class);

        assertEquals(2, ram.getMemory().getDataWord(256));
    }

}
