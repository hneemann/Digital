package de.neemann.digital.core.arithmetic;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class ComparatorTest extends TestCase {

    public void testCompUnsigned() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(4)
                .set(AttributeKey.Signed, false));
        node.setInputs(a, b);
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 1, 0, 0);
        sc.check(1, 2, 0, 0, 1);
        sc.check(14, 2, 1, 0, 0);
    }

    public void testCompSigned() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);


        Model model = new Model();
        Comparator node = new Comparator(new ElementAttributes()
                .setBits(4)
                .set(AttributeKey.Signed, true));
        node.setInputs(a, b);
        model.add(node);

        TestExecuter sc = new TestExecuter(model).setInputs(a, b).setOutputs(node.getOutputs());
        sc.check(0, 0, 0, 1, 0);
        sc.check(1, 0, 1, 0, 0);
        sc.check(1, 2, 0, 0, 1);
        sc.check(7, 2, 1, 0, 0);
        sc.check(8, 2, 0, 0, 1);
        sc.check(15, 2, 0, 0, 1);
        sc.check(13, 15, 0, 0, 1);
    }


}