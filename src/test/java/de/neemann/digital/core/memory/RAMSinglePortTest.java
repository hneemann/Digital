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

import static de.neemann.digital.TestExecuter.HIGHZ;
import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class RAMSinglePortTest extends TestCase {

    public void testRAM() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue str = new ObservableValue("str", 1);
        ObservableValue clk = new ObservableValue("clk", 1);
        ObservableValue ld = new ObservableValue("ld", 1);

        Model model = new Model();
        RAMSinglePort out = model.add(new RAMSinglePort(
                new ElementAttributes()
                        .set(Keys.ADDR_BITS, 4)
                        .setBits(4)));
        out.setInputs(ovs(a, str, clk, ld, d));

        TestExecuter sc = new TestExecuter(model).setInputs(a, d, str, clk, ld).setOutputs(out.getOutputs());
        //       A  D  ST C  LD
        sc.checkZ(0, 0, 0, 0, 0, HIGHZ);  // def
        sc.checkZ(0, 5, 1, 1, 0, HIGHZ);  // st  0->5
        sc.checkZ(0, 0, 0, 0, 0, HIGHZ);  // def
        sc.checkZ(1, 9, 1, 1, 0, HIGHZ);  // st  1->9
        sc.check(0, 0, 0, 0, 1, 5);      // rd  5
        sc.check(1, 0, 0, 0, 1, 9);      // rd  5
    }


}
