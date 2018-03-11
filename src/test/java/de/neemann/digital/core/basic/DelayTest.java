/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.basic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.Delay;
import junit.framework.TestCase;

/**
 */
public class DelayTest extends TestCase {

    public void testDelay() throws Exception {
        ObservableValue a = new ObservableValue("a", 2);

        Model model = new Model();
        Delay out = model.add(new Delay(new ElementAttributes().setBits(2)));
        out.setInputs(a.asList());

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 0);
        sc.check(1, 1);
        sc.check(2, 2);
        sc.check(3, 3);
    }


    public void testDelayVar() throws Exception {
        for (int delayTime = 0; delayTime < 5; delayTime++) {
            ObservableValue in = new ObservableValue("in", 1);

            Model model = new Model();
            Delay delay = model.add(new Delay(new ElementAttributes().set(Keys.DELAY_TIME, delayTime)));
            delay.setInputs(in.asList());
            assertEquals(1, delay.getOutputs().size());
            ObservableValue out = delay.getOutputs().get(0);

            model.init();

            in.setValue(0);
            model.doStep();
            assertEquals(0, out.getValue());

            in.setValue(1);
            for (int i = 1; i < delayTime; i++) {
                assertTrue(model.needsUpdate());
                model.doMicroStep(false);
                assertEquals(0, out.getValue());
            }
            assertTrue(model.needsUpdate());
            model.doMicroStep(false);
            assertEquals(1, out.getValue());
            assertFalse(model.needsUpdate());
        }
    }

}
