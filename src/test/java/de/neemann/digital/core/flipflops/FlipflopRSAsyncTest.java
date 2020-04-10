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

public class FlipflopRSAsyncTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue s = new ObservableValue("s", 1);
        ObservableValue r = new ObservableValue("r", 1);

        Model model = new Model();
        FlipflopRSAsync out = model.add(new FlipflopRSAsync(new ElementAttributes()));
        out.setInputs(ovs(s, r));

        TestExecuter sc = new TestExecuter(model).setInputs(s, r).setOutputs(out.getOutputs());
        //       S  R  Q  ~Q
        sc.check(0, 0, 0, 1);
        sc.check(1, 0, 1, 0);
        sc.check(0, 0, 1, 0);
        sc.check(0, 1, 0, 1);
        sc.check(0, 0, 0, 1);
        sc.check(1, 1, 0, 0);
        sc.check(1, 0, 1, 0);
        sc.check(1, 1, 0, 0);
        sc.check(0, 1, 0, 1);

        ObservableValue q = out.getOutputs().get(0);
        ObservableValue qn = out.getOutputs().get(1);

        for (int i = 0; i < 100; i++) {
            s.setValue(1);
            r.setValue(1);
            model.doStep();
            assertFalse(q.getBool());
            assertFalse(qn.getBool());
            s.setValue(0);
            r.setValue(0);
            model.doStep();
            assertTrue(q.getBool() ^ qn.getBool());
        }
    }

}
