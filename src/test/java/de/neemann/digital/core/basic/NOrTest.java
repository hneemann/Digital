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
public class NOrTest extends TestCase {

    public void testNOr() throws Exception {
        ObservableValue a = new ObservableValue("a", 1);
        ObservableValue b = new ObservableValue("b", 1);

        Model model = new Model();
        NOr nor = model.add(new NOr(new ElementAttributes().setBits(1)));
        nor.setInputs(ovs(a, b));

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(nor.getOutputs());
        sc.check(0, 0, 1);
        sc.check(1, 0, 0);
        sc.check(0, 1, 0);
        sc.check(1, 1, 0);
        sc.check(1, 0, 0);
        sc.check(0, 1, 0);
        sc.check(0, 0, 1);
    }

    public void testNOr64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);
        ObservableValue b = new ObservableValue("b", 64);

        Model model = new Model();
        FanIn and = model.add(new NOr(new ElementAttributes().setBits(64)));
        and.setInputs(ovs(a, b));

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(and.getOutput());
        sc.check(0xff00000000000000L, 0x2200000000000000L, 0x00ffffffffffffffL);
    }

}
