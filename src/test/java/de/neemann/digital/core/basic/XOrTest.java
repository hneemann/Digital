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

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class XOrTest extends TestCase {

    public void testXor() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);


        Model model = new Model();
        XOr out = model.add(new XOr(new ElementAttributes().setBits(1)));
        out.setInputs(ovs(a, b));

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(out.getOutputs());
        sc.check(0, 0, 0);
        sc.check(1, 0, 1);
        sc.check(0, 1, 1);
        sc.check(1, 1, 0);
    }

    public void testXor64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);
        ObservableValue b = new ObservableValue("b", 64);


        Model model = new Model();
        XOr out = model.add(new XOr(new ElementAttributes().setBits(64)));
        out.setInputs(ovs(a, b));

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(out.getOutputs());
        sc.check(0x7e00000000000000L, 0x2200000000000000L, 0x5c00000000000000L);
    }
}
