package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class JK_FFTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue j = new ObservableValue("j", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue k = new ObservableValue("k", 1);

        Model model = new Model();
        JK_FF out = model.add(new JK_FF(new ElementAttributes()));
        out.setInputs(j, c, k);

        TestExecuter sc = new TestExecuter(model).setInputs(j, c, k).setOutputs(out.getOutputs());
        //       J  C  K  Q  ~Q
        sc.check(0, 0, 0, 0, 1);
        sc.check(0, 0, 0, 0, 1);
        sc.check(1, 0, 0, 0, 1);
        sc.check(0, 1, 0, 0, 1);
        sc.check(1, 0, 0, 0, 1);
        sc.check(1, 1, 0, 1, 0);
        sc.check(1, 0, 1, 1, 0);
        sc.check(1, 1, 1, 0, 1);
        sc.check(1, 0, 1, 0, 1);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
        sc.check(0, 0, 0, 0, 1);
    }
}