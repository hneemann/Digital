/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 */
public class BitCountTest extends TestCase {


    public void testBitCount() throws Exception {
        ObservableValue a = new ObservableValue("a", 6);


        Model model = new Model();
        BitCount node = new BitCount(new ElementAttributes().setBits(6));
        node.setInputs(a.asList());
        model.add(node);

        ObservableValues outputs = node.getOutputs();
        assertEquals(1, outputs.size());
        assertEquals(3, outputs.get(0).getBits());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(outputs);
        sc.check(0, 0);
        sc.check(1, 1);
        sc.check(2, 1);
        sc.check(4, 1);
        sc.check(8, 1);
        sc.check(16, 1);
        sc.check(32, 1);
        sc.check(3, 2);
        sc.check(6, 2);
        sc.check(12, 2);
        sc.check(24, 2);
        sc.check(48, 2);
        sc.check(63, 6);
        sc.check(255, 6);
    }


    public void testBits() {
        assertEquals(1, new BitCount(new ElementAttributes().setBits(1)).getOutputs().get(0).getBits());
        assertEquals(2, new BitCount(new ElementAttributes().setBits(2)).getOutputs().get(0).getBits());
        assertEquals(3, new BitCount(new ElementAttributes().setBits(6)).getOutputs().get(0).getBits());
        assertEquals(3, new BitCount(new ElementAttributes().setBits(7)).getOutputs().get(0).getBits());
        assertEquals(4, new BitCount(new ElementAttributes().setBits(8)).getOutputs().get(0).getBits());
    }

    public void testBitCount64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);

        Model model = new Model();
        BitCount node = new BitCount(new ElementAttributes().setBits(64));
        node.setInputs(a.asList());
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(node.getOutputs());
        sc.check(0, 0);
        sc.check(-1, 64);
        sc.check(1, 1);
    }

}
