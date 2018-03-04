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
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class ComparatorTest extends TestCase {

    public void testCompUnsigned() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(4)
                .set(Keys.SIGNED, false));
        node.setInputs(ovs(a, b));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 1, 0, 0);
        sc.check(1, 2, 0, 0, 1);
        sc.check(14, 2, 1, 0, 0);
    }

    public void testCompSigned() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(4)
                .set(Keys.SIGNED, true));
        node.setInputs(ovs(a, b));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 1, 0, 0);
        sc.check(1, 2, 0, 0, 1);
        sc.check(7, 2, 1, 0, 0);
        sc.check(8, 2, 0, 0, 1);
        sc.check(15, 2, 0, 0, 1);
        sc.check(13, 15, 0, 0, 1);

        sc.check(-2, -3, 1, 0, 0);
        sc.check(-7, -8, 1, 0, 0);
    }

    public void testCompSigned64Bit() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);
        ObservableValue b = new ObservableValue("b", 64);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(64)
                .set(Keys.SIGNED, true));
        node.setInputs(ovs(a, b));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        //                    gr eq kl
        sc.check(0, 0, 0, 1, 0);
        sc.check(0, -2, 1, 0, 0);
        sc.check(-2, 0, 0, 0, 1);
        sc.check(-1, -2, 1, 0, 0);
        sc.check(-2, -1, 0, 0, 1);
        sc.check(0x8000000000000000L, 0, 0, 0, 1);
    }

    public void testCompUnsigned64Bit() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);
        ObservableValue b = new ObservableValue("b", 64);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(64)
                .set(Keys.SIGNED, false));
        node.setInputs(ovs(a, b));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        //                    gr eq kl
        sc.check(0, 0, 0, 1, 0);
        sc.check(0, -2, 0, 0, 1);
        sc.check(-2, 0, 1, 0, 0);
        sc.check(-1, -2, 1, 0, 0);
        sc.check(-2, -1, 0, 0, 1);
        sc.check(2, 1, 1, 0, 0);
        sc.check(0xC000000000000000L, 0x8000000000000000L, 1, 0, 0);
        sc.check(0x8000000000000000L, 0, 1, 0, 0);
    }

}
