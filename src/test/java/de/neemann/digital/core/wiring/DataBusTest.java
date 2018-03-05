/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.wiring.bus.DataBus;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

/**
 */
public class DataBusTest extends TestCase {

    public void testSimple() throws PinException, NodeException {
        ObservableValue a = new ObservableValue("a", 4)
                .setToHighZ();
        ObservableValue b = new ObservableValue("b", 4)
                .setToHighZ();

        Model m = new Model();

        ObservableValue out = new DataBus(null, m, a, b).getReadableOutput();

        a.setValue(1);
        assertEquals(1, out.getValue());
        a.setToHighZ();
        b.setValue(2);
        assertEquals(2, out.getValue());
        b.setToHighZ();

//        try {  ToDo HighZ
//            out.getValue();
//            assertTrue(false);
//        } catch (HighZException e) {
//            assertTrue(true);
//        }

        a.setValue(1);
        b.setValue(1);
        m.doStep();

        a.setValue(0);
        b.setValue(0);
        m.doStep();

        a.setValue(1);
        b.setValue(0);
        try {
            m.doStep();
            assertTrue(true);
        } catch (BurnException e) {
            assertTrue(true);
        }

    }

}
