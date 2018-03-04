/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class FlipflopDAsyncTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue set = new ObservableValue("set", 1);
        ObservableValue d = new ObservableValue("d", 8);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue clr = new ObservableValue("clr", 1);

        Model model = new Model();
        FlipflopDAsync out = model.add(new FlipflopDAsync(new ElementAttributes().setBits(8)));
        out.setInputs(ovs(set, d, c, clr));

        TestExecuter sc = new TestExecuter(model).setInputs(set, d, c, clr).setOutputs(out.getOutputs());
        //      set D  C  clr Q  ~Q
        sc.check(0, 0, 0, 0, 0, 255);
        sc.check(1, 0, 0, 0, 255, 0); // set
        sc.check(0, 0, 0, 0, 255, 0);
        sc.check(0, 0, 0, 1, 0, 255); // clr
        sc.check(0, 0, 0, 0, 0, 255);
        sc.check(0, 1, 0, 0, 0, 255);
        sc.check(0, 1, 1, 0, 1, 254);
        sc.check(0, 0, 0, 0, 1, 254);
    }
}
