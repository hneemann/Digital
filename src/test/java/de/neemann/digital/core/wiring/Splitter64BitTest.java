/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

public class Splitter64BitTest extends TestCase {

    public void test64Bit() throws NodeException, PinException {
        ObservableValue a = new ObservableValue("a", 16);
        ObservableValue b = new ObservableValue("b", 16);
        ObservableValue c = new ObservableValue("c", 16);
        ObservableValue d = new ObservableValue("d", 16);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "16,16,16,16")
                .set(Keys.OUTPUT_SPLIT, "64"));

        splitter.setInputs(ovs(d, c, b, a));

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(1, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a, b, c, d).setOutputsOf(splitter);
        sc.check(0x0, 0x0, 0x0, 0x0, 0x00);
        sc.check(0x1, 0x1, 0x1, 0x1, 0x0001000100010001L);
        sc.check(0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFFFFFFFFFFFFFFL);
        sc.check(0xFFFF, 0xFFFF, 0xFFFF, 0x0000, 0xFFFFFFFFFFFF0000L);
        sc.check(0xFFFF, 0xFFFF, 0x0000, 0xFFFF, 0xFFFFFFFF0000FFFFL);
        sc.check(0xFFFF, 0x0000, 0xFFFF, 0xFFFF, 0xFFFF0000FFFFFFFFL);
        sc.check(0x0000, 0xFFFF, 0xFFFF, 0xFFFF, 0x0000FFFFFFFFFFFFL);
    }

    public void test64Bit2() throws NodeException, PinException {
        ObservableValue a = new ObservableValue("a", 64);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "64")
                .set(Keys.OUTPUT_SPLIT, "16,16,16,16"));

        splitter.setInputs(ovs(a));

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(4, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a).setOutputsOf(splitter);
        sc.check(0x0, 0x0, 0x0, 0x0, 0x0);
        sc.check(-1, 0xffff, 0xffff, 0xffff, 0xffff);
        sc.check(0xffffffffffffffffL, 0xffff, 0xffff, 0xffff, 0xffff);
        sc.check(0xffffffffffff0000L, 0, 0xffff, 0xffff, 0xffff);
        sc.check(0xffffffff0000ffffL, 0xffff, 0, 0xffff, 0xffff);
        sc.check(0xffff0000ffffffffL, 0xffff, 0xffff, 0, 0xffff);
        sc.check(0x0000ffffffffffffL, 0xffff, 0xffff, 0xffff, 0);
    }
}
