/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.IGNORE;
import static de.neemann.digital.core.ObservableValues.ovs;

public class BusSplitterTest extends TestCase {

    public void testBusSplitter() throws Exception {
        Model model = new Model();
        ObservableValue oe = new ObservableValue("oe", 1);
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue d0 = new ObservableValue("d0", 1);
        ObservableValue d1 = new ObservableValue("d1", 1);
        ObservableValue d2 = new ObservableValue("d2", 1);
        ObservableValue d3 = new ObservableValue("d3", 1);
        BusSplitter out = model.add(new BusSplitter(
                new ElementAttributes()
                        .setBits(4)));
        out.setInputs(ovs(oe, d, d0, d1, d2, d3));


        TestExecuter te = new TestExecuter(model).setInputs(oe, d, d0, d1, d2, d3).setOutputs(out.getOutputs());
        te.checkZ(1, 0, 0, 0, 0, 0, IGNORE, 0, 0, 0, 0);
        te.checkZ(1, 5, 0, 0, 0, 0, IGNORE, 1, 0, 1, 0);
        te.checkZ(1, 15, 0, 0, 0, 0, IGNORE, 1, 1, 1, 1);
        te.checkZ(0, 0, 0, 0, 0, 0, 0, IGNORE, IGNORE, IGNORE, IGNORE);
        te.checkZ(0, 0, 1, 0, 1, 0, 5, IGNORE, IGNORE, IGNORE, IGNORE);
        te.checkZ(0, 0, 1, 1, 1, 1, 15, IGNORE, IGNORE, IGNORE, IGNORE);
    }

}
