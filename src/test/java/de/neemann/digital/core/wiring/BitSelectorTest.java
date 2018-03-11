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

public class BitSelectorTest extends TestCase {

    public void testBitSel() throws Exception {
        Model model = new Model();
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue sel = new ObservableValue("sel", 2);
        BitSelector out = model.add(new BitSelector(
                new ElementAttributes()
                        .set(Keys.SELECTOR_BITS, 2)));
        out.setInputs(ovs(d, sel));


        TestExecuter te = new TestExecuter(model).setInputs(d, sel).setOutputs(out.getOutputs());
        te.check(5, 0, 1);
        te.check(5, 1, 0);
        te.check(5, 2, 1);
        te.check(5, 3, 0);
        te.check(10, 0, 0);
        te.check(10, 1, 1);
        te.check(10, 2, 0);
        te.check(10, 3, 1);
    }


}
