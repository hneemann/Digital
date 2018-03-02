/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class ROMTest extends TestCase {

    public void testROM() throws Exception {
        ObservableValue addr = new ObservableValue("addr", 3);
        ObservableValue sel = new ObservableValue("sel", 1);

        Model model = new Model();
        DataField data = new DataField(8);
        data.setData(3, 17);
        data.setData(7, 200);
        ROM out = model.add(new ROM(
                new ElementAttributes()
                        .setBits(8)
                        .set(Keys.ADDR_BITS, 3)
                        .set(Keys.DATA, data)));
        out.setInputs(ovs(addr, sel));

        TestExecuter sc = new TestExecuter(model).setInputs(addr, sel).setOutputs(out.getOutputs());
        sc.check(0, 1, 0);
        sc.check(1, 1, 0);
        sc.check(2, 1, 0);
        sc.check(3, 1, 17);
        sc.check(4, 1, 0);
        sc.check(5, 1, 0);
        sc.check(6, 1, 0);

//        try {  ToDo HighZ
//            sc.check(6, 0, 0);
//            assertTrue(false);
//        } catch (HighZException e) {
//            assertTrue(true);
//        }

    }
}
