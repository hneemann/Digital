/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.memory.*;
import de.neemann.digital.core.memory.importer.Importer;
import de.neemann.digital.testing.UnitTester;
import junit.framework.TestCase;

import java.io.File;

public class UnitTestTest extends TestCase {

    public void testProcessor() throws Exception {
        // the file containing the processor
        File processor = new File(Resources.getRoot(),
                "../../main/dig/processor/Processor.dig");

        // the hex file containing the program
        File hexFile = new File(Resources.getRoot(),
                "programs/fibonacci.hex");

        // import the hex file
        DataField program = Importer.read(hexFile, 16);

        // run the program
        RAMInterface ram = new UnitTester(processor)    // load processor
                .writeDataTo(pm -> pm instanceof ROM && pm.getDataBits() == 16, program)
                // write program to the 16-Bit rom
                .runToBreak()                           // run program to break point
                .getRAM(v -> v instanceof RAMDualPort); // get the data ram of processor

        // get the content of the data ram
        DataField ramContent = ram.getMemory();

        // check if address 0 contains the value 610 ( the fifteenth Fibonacci number )
        assertEquals(610, ramContent.getDataWord(0));
    }
}
