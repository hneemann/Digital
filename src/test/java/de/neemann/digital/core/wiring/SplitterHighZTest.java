/*
 * Copyright (c) 2016 Helmut Neemann
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

import static de.neemann.digital.TestExecuter.HIGHZ;
import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class SplitterHighZTest extends TestCase {

    public void testHighZError() throws NodeException, PinException {
        ObservableValue a = new ObservableValue("a", 1)
                .setToHighZ();
        ObservableValue b = new ObservableValue("b", 1);

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "1,1")
                .set(Keys.OUTPUT_SPLIT, "2"));

        splitter.setInputs(ovs(a, b));

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(1, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a, b).setOutputsOf(splitter);
        sc.check(0, 0, 0);
        sc.check(0, 1, 2);
        sc.check(1, 0, 1);
        sc.check(1, 1, 3);
        sc.checkZ(HIGHZ, HIGHZ, HIGHZ);
    }

    public void testHighZEnabled() throws NodeException, PinException {
        ObservableValue a = new ObservableValue("a", 2)
                .setToHighZ();

        Splitter splitter = new Splitter(new ElementAttributes()
                .set(Keys.INPUT_SPLIT, "2")
                .set(Keys.OUTPUT_SPLIT, "1,1"));

        splitter.setInputs(ovs(a));

        ObservableValues outputs = splitter.getOutputs();
        assertEquals(2, outputs.size());

        TestExecuter sc = new TestExecuter().setInputs(a).setOutputsOf(splitter);
        sc.check(0, 0, 0);
        sc.check(1, 1, 0);
        sc.check(2, 0, 1);
        sc.check(3, 1, 1);
        sc.checkZ(HIGHZ, HIGHZ, HIGHZ);
    }

}
