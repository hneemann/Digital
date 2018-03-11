/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class RegisterTest extends TestCase {

    public void testRegister() throws Exception {
        ObservableValue d = new ObservableValue("s", 8);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue en = new ObservableValue("en", 1);

        Model model = new Model();
        Register out = model.add(new Register(new ElementAttributes().setBits(8)));
        out.setInputs(ovs(d, c, en));

        TestExecuter sc = new TestExecuter(model).setInputs(d, c, en).setOutputs(out.getOutputs());
        //       D  C  en  Q
        sc.check(0, 0, 0, 0);
        sc.check(7, 1, 1, 7);
        sc.check(9, 1, 1, 7);
        sc.check(9, 1, 0, 7);
        sc.check(9, 0, 0, 7);
        sc.check(9, 0, 1, 7);
        sc.check(9, 1, 1, 9);
        sc.check(0, 0, 0, 9);
        sc.check(0, 1, 0, 9);
        sc.check(0, 1, 1, 9);
        sc.check(0, 0, 0, 9);
        sc.check(0, 1, 0, 9);
        sc.check(0, 0, 1, 9);
    }


}
