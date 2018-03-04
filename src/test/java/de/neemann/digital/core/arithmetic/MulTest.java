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

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class MulTest extends TestCase {
    public void testMul() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);


        Model model = new Model();
        Mul node = model.add(new Mul(new ElementAttributes().setBits(4)));
        node.setInputs(ovs(a, b));

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputsOf(node);
        sc.check(0, 0, 0);
        sc.check(6, 6, 36);
        sc.check(15, 15, 225);
    }
}
