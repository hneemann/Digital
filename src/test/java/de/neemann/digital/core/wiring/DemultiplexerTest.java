/*
 * Copyright (c) 2016 Helmut Neemann
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

/**
 */
public class DemultiplexerTest extends TestCase {

    public void testDemux() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue sel = new ObservableValue("sel", 2);
        Demultiplexer demul = model.add(new Demultiplexer(
                new ElementAttributes()
                        .set(Keys.BITS, 4)
                        .set(Keys.SELECTOR_BITS, 2)));
        demul.setInputs(ovs(sel, a));


        TestExecuter te = new TestExecuter(model).setInputs(a, sel).setOutputs(demul.getOutputs());
        te.check(2, 0, 2, 0, 0, 0);
        te.check(3, 0, 3, 0, 0, 0);
        te.check(3, 1, 0, 3, 0, 0);
        te.check(4, 2, 0, 0, 4, 0);
        te.check(5, 3, 0, 0, 0, 5);
    }

    public void testDemuxDefault() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue sel = new ObservableValue("sel", 2);
        Demultiplexer demul = model.add(new Demultiplexer(
                new ElementAttributes()
                        .set(Keys.BITS, 4)
                        .set(Keys.DEFAULT, 7L)
                        .set(Keys.SELECTOR_BITS, 2)));
        demul.setInputs(ovs(sel, a));


        TestExecuter te = new TestExecuter(model).setInputs(a, sel).setOutputs(demul.getOutputs());
        te.check(2, 0, 2, 7, 7, 7);
        te.check(3, 0, 3, 7, 7, 7);
        te.check(3, 1, 7, 3, 7, 7);
        te.check(4, 2, 7, 7, 4, 7);
        te.check(5, 3, 7, 7, 7, 5);
    }


}
