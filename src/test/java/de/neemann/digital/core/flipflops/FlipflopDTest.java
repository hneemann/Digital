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
public class FlipflopDTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue d = new ObservableValue("d", 8);
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        FlipflopD out = model.add(new FlipflopD(new ElementAttributes().setBits(8)));
        out.setInputs(ovs(d, c));

        TestExecuter sc = new TestExecuter(model).setInputs(d, c).setOutputs(out.getOutputs());
        //       D  C  Q  ~Q
        sc.check(0, 0, 0, 255);
        sc.check(1, 0, 0, 255);
        sc.check(1, 1, 1, 254);
        sc.check(1, 0, 1, 254);
        sc.check(0, 0, 1, 254);
        sc.check(7, 0, 1, 254);
        sc.check(7, 1, 7, 248);
    }
}
