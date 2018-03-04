/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 */
public class NegTest extends TestCase {

    public void testNeg() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);

        Model model = new Model();
        Neg out = model.add(new Neg(new ElementAttributes().setBits(4)));
        out.setInputs(a.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 0);
        sc.check(1, 15);
        sc.check(15, 1);
        sc.check(3, 13);
        sc.check(7, 9);
        sc.check(8, 8);
        sc.check(9, 7);
    }

    public void testNeg64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);

        Model model = new Model();
        Neg out = model.add(new Neg(new ElementAttributes().setBits(64)));
        out.setInputs(a.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 0);
        sc.check(-1, 1);
        sc.check(1, -1);
    }


}
