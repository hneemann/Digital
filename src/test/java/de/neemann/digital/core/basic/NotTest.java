/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.basic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 */
public class NotTest extends TestCase {

    public void testNot() throws Exception {
        ObservableValue a = new ObservableValue("a", 2);

        Model model = new Model();
        Not out = model.add(new Not(new ElementAttributes().setBits(2)));
        out.setInputs(a.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 3);
        sc.check(1, 2);
        sc.check(2, 1);
        sc.check(3, 0);
    }

    public void testNot64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);

        Model model = new Model();
        Not out = model.add(new Not(new ElementAttributes().setBits(64)));
        out.setInputs(a.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0xff00000000000000L, 0x00ffffffffffffffL);
    }
}
