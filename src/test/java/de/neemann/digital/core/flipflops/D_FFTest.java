package de.neemann.digital.core.flipflops;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.PartAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class D_FFTest extends TestCase {
    public void testFlipFlop() throws Exception {
        ObservableValue d = new ObservableValue("s", 8);
        ObservableValue c = new ObservableValue("c", 1);

        Model model = new Model();
        D_FF out = model.add(new D_FF(new PartAttributes().setBits(8)));
        out.setInputs(d, c);

        TestExecuter sc = new TestExecuter(model).setInputs(d, c).setOutputs(out.getOutputs());
        //       D  C  Q  ~Q
        sc.check(0, 0, 0, 255);
        sc.check(1, 0, 0, 255);
        sc.check(1, 1, 1, 254);
        sc.check(1, 0, 1, 254);
        sc.check(0, 0, 1, 254);
        sc.check(7, 0, 1, 254);
        sc.check(7, 1, 7, 248);
    }
}