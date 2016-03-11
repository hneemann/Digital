package de.neemann.digital.basic;

import de.neemann.digital.Model;
import de.neemann.digital.ObservableValue;
import de.neemann.digital.TestExecuter;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class OrTest extends TestCase {

    public void testOr() throws Exception {
        ObservableValue a = new ObservableValue(1);
        ObservableValue b = new ObservableValue(1);

        Model model = new Model();
        Function and = model.add(new Or(1)).addInput(a).addInput(b);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(and.getOutput());
        sc.check(0, 0, 0);
        sc.check(1, 0, 1);
        sc.check(0, 1, 1);
        sc.check(1, 1, 1);
        sc.check(1, 0, 1);
        sc.check(0, 1, 1);
        sc.check(0, 0, 0);
    }
}