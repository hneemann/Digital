/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.TestExecuter.IGNORE;
import static de.neemann.digital.core.ObservableValues.ovs;

/**
 */
public class FlipFlopsTest extends TestCase {

    public void testFlipFlopNOr() throws Exception {
        ObservableValue r = new ObservableValue("r", 1);
        ObservableValue s = new ObservableValue("s", 1);

        Model model = new Model();
        FanIn a1 = model.add(new NOr(new ElementAttributes().setBits(1)));
        FanIn a2 = model.add(new NOr(new ElementAttributes().setBits(1)));

        a1.setInputs(ovs(r, a2.getOutput()));
        a2.setInputs(ovs(s, a1.getOutput()));

        TestExecuter sc = new TestExecuter(model, true).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(0, 1, 1, 0);
        sc.check(0, 0, 1, 0);
        sc.check(1, 0, 0, 1);
        sc.check(0, 0, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 0, 0);   // verbotener Zustand!!
        r.setValue(0);          // gehe aus verbotenem Zustand raus!!!
        s.setValue(0);
        model.doStep(true);     // geht nur mit noise!

        assertTrue((a1.getOutput().getValue() == 1 && a2.getOutput().getValue() == 0) ||  // endzustand ist undefiniert!
                (a1.getOutput().getValue() == 0 && a2.getOutput().getValue() == 1));
    }

    public void testFlipFlopNAnd() throws Exception {
        ObservableValue r = new ObservableValue("r", 1);
        ObservableValue s = new ObservableValue("s", 1);

        Model model = new Model();
        FanIn a1 = model.add(new NAnd(new ElementAttributes().setBits(1)));
        FanIn a2 = model.add(new NAnd(new ElementAttributes().setBits(1)));

        a1.setInputs(ovs(r, a2.getOutput()));
        a2.setInputs(ovs(s, a1.getOutput()));

        TestExecuter sc = new TestExecuter(model).setInputs(r, s).setOutputs(a1.getOutput(), a2.getOutput());
        sc.check(1, 0, 0, 1);
        sc.check(1, 1, 0, 1);
        sc.check(0, 1, 1, 0);
        sc.check(1, 1, 1, 0);
        sc.check(1, 0, 0, 1);
    }


    public void testFlipFlopJKMS() throws Exception {
        ObservableValue j = new ObservableValue("j", 1);
        ObservableValue k = new ObservableValue("k", 1);
        ObservableValue c = new ObservableValue("c", 1);

        ElementAttributes attr = new ElementAttributes().setBits(1);

        Model model = new Model();
        FanIn nor3 = model.add(new NOr(attr));
        FanIn nor4 = model.add(new NOr(attr));

        FanIn a1 = model.add(new And(attr));
        a1.setInputs(ovs(j, c, nor4.getOutput()));
        FanIn a2 = model.add(new And(attr));
        a2.setInputs(ovs(k, c, nor3.getOutput()));
        Not not = model.add(new Not(attr));
        not.setInputs(c.asList());

        FanIn nor1 = model.add(new NOr(attr));
        FanIn nor2 = model.add(new NOr(attr));

        nor1.setInputs(ovs(a1.getOutput(), nor2.getOutput()));
        nor2.setInputs(ovs(a2.getOutput(), nor1.getOutput()));


        FanIn a3 = model.add(new And(attr));
        a3.setInputs(ovs(nor1.getOutput(), not.getOutputs().get(0)));
        FanIn a4 = model.add(new And(attr));
        a4.setInputs(ovs(nor2.getOutput(), not.getOutputs().get(0)));

        nor3.setInputs(ovs(a3.getOutput(), nor4.getOutput()));
        nor4.setInputs(ovs(a4.getOutput(), nor3.getOutput()));

        TestExecuter sc = new TestExecuter(model, true).setInputs(c, j, k).setOutputs(nor3.getOutput(), nor4.getOutput());
        sc.checkZ(0, 1, 0, IGNORE, IGNORE); // undefined
        sc.checkZ(1, 1, 0, IGNORE, IGNORE); // undefined
        sc.check(0, 1, 0, 1, 0);
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 0, 1, 0);
        sc.check(0, 1, 0, 1, 0);
        sc.check(1, 1, 0, 1, 0);
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 0, 1, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(1, 0, 1, 1, 0);
        sc.check(0, 0, 1, 0, 1);

        sc.check(0, 1, 1, 0, 1);
        sc.check(1, 1, 1, 0, 1);
        sc.check(0, 1, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
        sc.check(1, 1, 1, 0, 1);
        sc.check(0, 1, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
    }
}
