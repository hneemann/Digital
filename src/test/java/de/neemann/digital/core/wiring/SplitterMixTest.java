/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class SplitterMixTest extends TestCase {

    public void test1() throws Exception {
        ObservableValue a = new ObservableValue("a", 8);
        ObservableValue b = new ObservableValue("b", 8);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "8,8")
                .set(Keys.OUTPUT_SPLIT, "4,12"));

        splitter.setInputs(ovs(a, b));
        assertEquals(2, a.observerCount());
        assertEquals(1, b.observerCount());

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(2, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0x00, 0x00, 0x0, 0x000);

        sc.check(0x01, 0x00, 0x1, 0x000);
        sc.check(0x10, 0x00, 0x0, 0x001);
        sc.check(0x00, 0x01, 0x0, 0x010);
        sc.check(0x00, 0x10, 0x0, 0x100);

        sc.check(0x0f, 0x00, 0xf, 0x000);
        sc.check(0xf0, 0x00, 0x0, 0x00f);
        sc.check(0x00, 0x0f, 0x0, 0x0f0);
        sc.check(0x00, 0xf0, 0x0, 0xf00);

        sc.check(0xc0, 0xab, 0x0, 0xabc);
    }

    public void test2() throws Exception {
        ObservableValue a = new ObservableValue("a", 8);
        ObservableValue b = new ObservableValue("b", 8);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "8,8")
                .set(Keys.OUTPUT_SPLIT, "12,4"));

        splitter.setInputs(ovs(a, b));
        assertEquals(1, a.observerCount());
        assertEquals(2, b.observerCount());

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(2, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0x00, 0x00, 0x000, 0x0);

        sc.check(0x01, 0x00, 0x001, 0x0);
        sc.check(0x10, 0x00, 0x010, 0x0);
        sc.check(0x00, 0x01, 0x100, 0x0);
        sc.check(0x00, 0x10, 0x000, 0x1);

        sc.check(0x0f, 0x00, 0x00f, 0x0);
        sc.check(0xf0, 0x00, 0x0f0, 0x0);
        sc.check(0x00, 0x0f, 0xf00, 0x0);
        sc.check(0x00, 0xf0, 0x000, 0xf);

        sc.check(0xbc, 0xda, 0xabc, 0xd);
    }

    public void test3() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 4);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "4,4,4")
                .set(Keys.OUTPUT_SPLIT, "2,8,2"));

        splitter.setInputs(ovs(a, b, c));
        assertEquals(2, a.observerCount());
        assertEquals(1, b.observerCount());
        assertEquals(2, c.observerCount());

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(3, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a, b, c).setOutputsOf(splitter);
        sc.check(0x0, 0x0, 0x0, 0x0, 0x00, 0x0);
        sc.check(0xf, 0x0, 0x0, 0x3, 0x03, 0x0);
        sc.check(0x0, 0xf, 0x0, 0x0, 0x3c, 0x0);
        sc.check(0x0, 0x0, 0xf, 0x0, 0xc0, 0x3);
        sc.check(0xf, 0xf, 0xf, 0x3, 0xff, 0x3);
    }


}
