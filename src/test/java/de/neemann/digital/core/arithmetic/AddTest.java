package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.PartAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class AddTest extends TestCase {

    public void testAdd() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 1);


        Model model = new Model();
        Add node = new Add(new PartAttributes().bits(4));
        node.setInputs(a, b, c);
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b, c).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 0, 0);
        sc.check(0, 0, 1, 1, 0);
        sc.check(2, 3, 0, 5, 0);
        sc.check(2, 3, 1, 6, 0);
        sc.check(7, 7, 0, 14, 0);
        sc.check(8, 8, 0, 0, 1);
        sc.check(8, 8, 1, 1, 1);
    }
}