package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

import static de.neemann.digital.core.ObservableValues.ovs;

/**
 * @author hneemann
 */
public class SubTest extends TestCase {

    public void testSub() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Sub(new ElementAttributes().setBits(4));
        node.setInputs(ovs(a, b, c));
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputsOf(node);
        sc.check(0, 0, 0, 0, 0);
        sc.check(3, 2, 0, 1, 0);
        sc.check(2, 3, 0, 15, 1);
        sc.check(3, 3, 0, 0, 0);
        sc.check(3, 3, 1, 15, 1);
    }
}