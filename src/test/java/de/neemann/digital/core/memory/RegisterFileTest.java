/*
 * Copyright (c) 2018 Helmut Neemann
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
public class RegisterFileTest extends TestCase {

    public void testRegisterFile() throws Exception {
        ObservableValue ra = new ObservableValue("ra", 2);
        ObservableValue rb = new ObservableValue("rb", 2);
        ObservableValue rw = new ObservableValue("rw", 2);
        ObservableValue in = new ObservableValue("in", 4);
        ObservableValue we = new ObservableValue("we", 1);
        ObservableValue clk = new ObservableValue("clk", 1);

        Model model = new Model();
        RegisterFile out = model.add(new RegisterFile(
                new ElementAttributes()
                        .set(Keys.ADDR_BITS, 2)
                        .setBits(4)));
        out.setInputs(ovs(in, we, rw, clk, ra, rb));

        TestExecuter sc = new TestExecuter(model).setInputs(clk, in, we, rw, ra, rb).setOutputs(out.getOutputs());
        //              c  in we rw ra rb da db
        sc.check(0, 0, 0, 0, 0, 0, 0, 0);
        sc.check(1, 7, 1, 1, 0, 0, 0, 0);
        sc.check(0, 0, 0, 0, 1, 1, 7, 7);
        sc.check(1, 5, 1, 2, 1, 1, 7, 7);
        sc.check(0, 0, 0, 0, 1, 2, 7, 5);
    }


}
