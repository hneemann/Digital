/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

/**
 */
public class FlipflopTTest extends TestCase {

    public void testFlipFlop() throws Exception {
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        FlipflopT out = model.add(new FlipflopT(new ElementAttributes().setBits(1).set(Keys.WITH_ENABLE, false)));
        out.setInputs(c.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(c).setOutputs(out.getOutputs());
        //       C  Q  ~Q
        sc.check(0, 0, 1);
        sc.check(1, 1, 0);
        sc.check(1, 1, 0);
        sc.check(0, 1, 0);
        sc.check(0, 1, 0);
        sc.check(1, 0, 1);
        sc.check(0, 0, 1);
    }

    public void testFlipFlopWithEnable() throws Exception {
        ObservableValue t = new ObservableValue("t", 1);
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        FlipflopT out = model.add(new FlipflopT(new ElementAttributes().setBits(1).set(Keys.WITH_ENABLE, true)));
        out.setInputs(new ObservableValues(t, c));

        TestExecuter sc = new TestExecuter(model).setInputs(t, c).setOutputs(out.getOutputs());
        //       T  C  Q  ~Q
        sc.check(1, 0, 0, 1);
        sc.check(1, 1, 1, 0);
        sc.check(1, 1, 1, 0);
        sc.check(1, 0, 1, 0);
        sc.check(1, 0, 1, 0);
        sc.check(1, 1, 0, 1);
        sc.check(1, 0, 0, 1);
        sc.check(0, 1, 0, 1);
        sc.check(0, 0, 0, 1);
        sc.check(0, 1, 0, 1);
        sc.check(0, 0, 0, 1);
        sc.check(1, 1, 1, 0);
        sc.check(0, 0, 1, 0);
        sc.check(0, 1, 1, 0);
        sc.check(0, 0, 1, 0);
    }

}
