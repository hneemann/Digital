package de.neemann.digital.core.basic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.wiring.Delay;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class DelayTest extends TestCase {

    public void testDelay() throws Exception {
        ObservableValue a = new ObservableValue("a", 2);

        Model model = new Model();
        Delay out = model.add(new Delay(new ElementAttributes().setBits(2)));
        out.setInputs(a);

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(out.getOutputs());
        sc.check(0, 0);
        sc.check(1, 1);
        sc.check(2, 2);
        sc.check(3, 3);
    }
}