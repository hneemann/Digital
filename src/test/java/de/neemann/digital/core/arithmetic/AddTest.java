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
public class AddTest extends TestCase {

    public void testAdd() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Add(new ElementAttributes().setBits(4));
        node.setInputs(ovs(a, b, c));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 0, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(2, 3, 0, 5, 0);
        sc.check(2, 3, 1, 6, 0);
        sc.check(7, 7, 0, 14, 0);
        sc.check(8, 8, 0, 0, 1);
        sc.check(8, 8, 1, 1, 1);
    }

    public void testAdd31() throws Exception {
        ObservableValue a = new ObservableValue("a", 31);
        ObservableValue b = new ObservableValue("b", 31);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Add(new ElementAttributes().setBits(31));
        node.setInputs(ovs(a, b, c));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(node.getOutputs());
        sc.check(-1, 0, 1, 0, 1);
        sc.check(-1, 1, 0, 0, 1);
    }

    public void testAdd32() throws Exception {
        ObservableValue a = new ObservableValue("a", 32);
        ObservableValue b = new ObservableValue("b", 32);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Add(new ElementAttributes().setBits(32));
        node.setInputs(ovs(a, b, c));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(node.getOutputs());
        sc.check(-1, 0, 1, 0, 1);
        sc.check(-1, 1, 0, 0, 1);
    }

    public void testAdd64() throws Exception {
        ObservableValue a = new ObservableValue("a", 64);
        ObservableValue b = new ObservableValue("b", 64);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Add(new ElementAttributes().setBits(64));
        node.setInputs(ovs(a, b, c));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(node.getOutputs());
        sc.check(-1, 0, 1, 0, 1);
        sc.check(-1, 1, 0, 0, 1);
    }
}
