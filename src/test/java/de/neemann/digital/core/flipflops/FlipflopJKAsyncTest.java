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
public class FlipflopJKAsyncTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue set = new ObservableValue("set", 1);
        ObservableValue j = new ObservableValue("j", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue k = new ObservableValue("k", 1);
        ObservableValue clr = new ObservableValue("clr", 1);

        Model model = new Model();
        FlipflopJKAsync out = model.add(new FlipflopJKAsync(new ElementAttributes()));
        out.setInputs(ovs(set, j, c, k, clr));

        TestExecuter sc = new TestExecuter(model).setInputs(set, j, c, k, clr).setOutputs(out.getOutputs());
        //       Set J  C  K  clr Q  ~Q
        sc.check(0, 0, 0, 0, 0, 0, 1);
        sc.check(0, 0, 0, 0, 0, 0, 1);
        sc.check(0, 1, 0, 0, 0, 0, 1);
        sc.check(0, 0, 1, 0, 0, 0, 1);
        sc.check(0, 1, 0, 0, 0, 0, 1);
        sc.check(0, 1, 1, 0, 0, 1, 0);
        sc.check(0, 1, 0, 1, 0, 1, 0);
        sc.check(0, 1, 1, 1, 0, 0, 1);
        sc.check(0, 1, 0, 1, 0, 0, 1);
        sc.check(0, 1, 1, 1, 0, 1, 0);
        sc.check(0, 0, 0, 1, 0, 1, 0);
        sc.check(0, 0, 1, 1, 0, 0, 1);
        sc.check(0, 0, 0, 0, 0, 0, 1);
        // async
        sc.check(1, 0, 0, 0, 0, 1, 0);
        sc.check(0, 0, 0, 0, 0, 1, 0);
        sc.check(0, 0, 0, 0, 1, 0, 1);
        sc.check(0, 0, 0, 0, 0, 0, 1);
    }
}
