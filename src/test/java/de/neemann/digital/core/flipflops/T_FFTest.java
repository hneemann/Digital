package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.PartAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class T_FFTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        T_FF out = model.add(new T_FF(new PartAttributes().setBits(1)));
        out.setInputs(c);

        TestExecuter sc = new TestExecuter(model).setInputs(c).setOutputs(out.getOutputs());
        //       C  Q  ~Q
        sc.check(0, 0, 1);
        sc.check(1, 1, 0);
        sc.check(1, 1, 0);
        sc.check(0, 1, 0);
        sc.check(0, 1, 0);
        sc.check(1, 0, 1);
        sc.check(0, 0, 1);
    }
}