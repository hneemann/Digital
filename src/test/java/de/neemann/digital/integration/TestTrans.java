/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.basic.XOr;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * Tests the handling of transparent circuits which only connect an input to an output.
 * <p>
 */
public class TestTrans extends TestCase {

    public void testTrans() throws Exception {
        Model model = new ToBreakRunner("dig/test/transp/transtest3.dig").getModel();
        assertEquals(2, model.getInputs().size());
        assertEquals(1, model.getOutputs().size());
        assertEquals(1, model.getNodes().size());

        Node node = model.getNodes().get(0);
        assertTrue(node instanceof XOr);
        XOr xor = (XOr) node;

        // The models inputs are the xor input values!
        // All the intermediate transparent stuff is removed!
        ArrayList<ObservableValue> ins = new ArrayList<>();
        for (Signal s : model.getInputs())
            ins.add(s.getValue());
        assertTrue(ins.contains(xor.getInputs().get(0)));
        assertTrue(ins.contains(xor.getInputs().get(1)));
    }

}
