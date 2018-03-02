/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

public class PriorityEncoderTest extends TestCase {

    public void testEncoder2() throws Exception {
        Model model = new Model();
        ObservableValue d0 = new ObservableValue("d0", 1);
        ObservableValue d1 = new ObservableValue("d1", 1);
        PriorityEncoder out = model.add(new PriorityEncoder(
                new ElementAttributes()
                        .set(Keys.SELECTOR_BITS, 1)));
        out.setInputs(ovs(d0, d1));

        TestExecuter te = new TestExecuter(model).setInputs(d1, d0).setOutputs(out.getOutputs());
        te.check(0, 0, 0, 0);
        te.check(0, 1, 0, 1);
        te.check(1, 0, 1, 1);
        te.check(1, 1, 1, 1);
    }

    public void testEncoder4() throws Exception {
        Model model = new Model();
        ObservableValue d0 = new ObservableValue("d0", 1);
        ObservableValue d1 = new ObservableValue("d1", 1);
        ObservableValue d2 = new ObservableValue("d2", 1);
        ObservableValue d3 = new ObservableValue("d3", 1);
        PriorityEncoder out = model.add(new PriorityEncoder(
                new ElementAttributes()
                        .set(Keys.SELECTOR_BITS, 2)));
        out.setInputs(ovs(d0, d1, d2, d3));

        TestExecuter te = new TestExecuter(model).setInputs(d3, d2, d1, d0).setOutputs(out.getOutputs());
        te.check(0, 0, 0, 0, 0, 0);
        te.check(0, 0, 0, 1, 0, 1);
        te.check(0, 0, 1, 0, 1, 1);
        te.check(0, 0, 1, 1, 1, 1);
        te.check(0, 1, 0, 0, 2, 1);
        te.check(0, 1, 0, 1, 2, 1);
        te.check(0, 1, 1, 0, 2, 1);
        te.check(0, 1, 1, 1, 2, 1);
        te.check(1, 0, 0, 0, 3, 1);
        te.check(1, 0, 0, 1, 3, 1);
        te.check(1, 0, 1, 0, 3, 1);
        te.check(1, 0, 1, 1, 3, 1);
        te.check(1, 1, 0, 0, 3, 1);
        te.check(1, 1, 0, 1, 3, 1);
        te.check(1, 1, 1, 0, 3, 1);
        te.check(1, 1, 1, 1, 3, 1);
    }

}
