package de.neemann.digital.core.basic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class NOrTest extends TestCase {

    public void testNOr() throws Exception {
        ObservableValue a = new ObservableValue(1);
        ObservableValue b = new ObservableValue(1);

        Model model = new Model();
        ObservableValue nor = model.add(new NOr(1)).addInput(a).addInput(b).getOutput();

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(nor);
        sc.check(0, 0, 1);
        sc.check(1, 0, 0);
        sc.check(0, 1, 0);
        sc.check(1, 1, 0);
        sc.check(1, 0, 0);
        sc.check(0, 1, 0);
        sc.check(0, 0, 1);
    }
}