/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.File;
import java.util.List;

public class ProgramMemoryLoaderTest extends TestCase {

    public void testSimple() throws Exception {
        ToBreakRunner runner = new ToBreakRunner("dig/testProgLoader.dig", false);
        Model model = runner.getModel();
        File romHex = new File(Resources.getRoot(), "dig/testProgLoader.hex");
        new ProgramMemoryLoader(romHex).preInit(model);
        model.init();

        List<RAMDualPort> ramList = model.findNode(RAMDualPort.class);
        assertEquals(1, ramList.size());
        RAMDualPort ram = ramList.get(0);

        assertEquals(0x55, ram.getMemory().getDataWord(0));
        assertEquals(0xAA, ram.getMemory().getDataWord(1));
        assertEquals(0x56, ram.getMemory().getDataWord(2));
        assertEquals(0xAB, ram.getMemory().getDataWord(3));
    }

    public void testSplit() throws Exception {
        ToBreakRunner runner = new ToBreakRunner("dig/testProgLoaderSplit.dig", false);
        Model model = runner.getModel();
        File romHex = new File(Resources.getRoot(), "dig/testProgLoader.hex");
        new ProgramMemoryLoader(romHex).preInit(model);
        model.init();

        List<RAMDualPort> ramList = model.findNode(RAMDualPort.class);
        assertEquals(2, ramList.size());
        RAMDualPort ram0 = ramList.get(0);
        RAMDualPort ram1 = ramList.get(1);

        assertEquals("R0", ram0.getLabel());
        assertEquals("R1", ram1.getLabel());

        assertEquals(0x55, ram0.getMemory().getDataWord(0));
        assertEquals(0xAA, ram1.getMemory().getDataWord(0));
        assertEquals(0x56, ram0.getMemory().getDataWord(1));
        assertEquals(0xAB, ram1.getMemory().getDataWord(1));
    }

    public void testSplitErr() throws Exception {
        ToBreakRunner runner = new ToBreakRunner("dig/testProgLoaderSplitErr1.dig", false);
        Model model = runner.getModel();
        File romHex = new File(Resources.getRoot(), "dig/testProgLoader.hex");

        try {
            new ProgramMemoryLoader(romHex).preInit(model);
            fail();
        } catch (NodeException e) {
            assertTrue(true);
        }
    }
}