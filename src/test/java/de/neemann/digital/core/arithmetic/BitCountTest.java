package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * Created by hneemann on 09.08.16.
 */
public class BitCountTest extends TestCase {

    public void testBitCount() throws Exception {
        ObservableValue a = new ObservableValue("a", 6);
        ObservableValue b = new ObservableValue("b", 3);


        Model model = new Model();
        BitCount node = new BitCount(new ElementAttributes().setBits(6));
        node.setInputs(a.asList());
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a).setOutputs(node.getOutputs());
        sc.check(0, 0);
        sc.check(1, 1);
        sc.check(2, 1);
        sc.check(4, 1);
        sc.check(8, 1);
        sc.check(16, 1);
        sc.check(32, 1);
        sc.check(3, 2);
        sc.check(6, 2);
        sc.check(12, 2);
        sc.check(24, 2);
        sc.check(48, 2);
        sc.check(63, 6);
        sc.check(255, 6);
    }


}