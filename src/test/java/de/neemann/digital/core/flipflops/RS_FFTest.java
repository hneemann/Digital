package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class RS_FFTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue s = new ObservableValue("s", 1);
        ObservableValue c = new ObservableValue("c", 1);
        ObservableValue r = new ObservableValue("r", 1);

        Model model = new Model();
        RS_FF out = model.add(new RS_FF(new ElementAttributes()));
        out.setInputs(s, c, r);

        TestExecuter sc = new TestExecuter(model).setInputs(s, c, r).setOutputs(out.getOutputs());
        //       S  C  R  Q  ~Q
        sc.check(0, 0, 0, 0, 1);
        sc.check(0, 0, 0, 0, 1);
        sc.check(1, 0, 0, 0, 1);
        sc.check(0, 1, 0, 0, 1);
        sc.check(1, 0, 0, 0, 1);
        sc.check(1, 1, 0, 1, 0);
        sc.check(1, 0, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(1, 0, 1, 1, 0);
        sc.check(1, 1, 1, 1, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(0, 1, 1, 0, 1);
        sc.check(0, 0, 0, 0, 1);
    }
}