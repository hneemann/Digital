/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui;

import de.neemann.digital.core.Model;
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
    }
}