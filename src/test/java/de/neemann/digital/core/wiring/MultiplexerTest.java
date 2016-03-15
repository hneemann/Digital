package de.neemann.digital.core.wiring;

import de.neemann.digital.TestExecuter;
import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.basic.FanIn;
import junit.framework.TestCase;

/**
 * @author hneemann
 */
public class MultiplexerTest extends TestCase {

    public void testMux() throws Exception {
        Model model = new Model();
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 4);
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue sel = new ObservableValue("sel", 2);
        FanIn out = model.add(new Multiplexer(4, 2));
        out.setInputs(sel, a, b, c, d);


        TestExecuter te = new TestExecuter(model).setInputs(a, b, c, d, sel).setOutputs(out.getOutputs());
        te.check(3, 4, 5, 6, 0, 3);
        te.check(3, 4, 5, 6, 1, 4);
        te.check(3, 4, 5, 6, 2, 5);
        te.check(3, 4, 5, 6, 3, 6);

    }

    public void testMux2() throws Exception {
        ObservableValue a = new ObservableValue("a", 4);
        ObservableValue b = new ObservableValue("b", 4);
        ObservableValue c = new ObservableValue("c", 4);
        ObservableValue d = new ObservableValue("d", 4);
        ObservableValue sel = new ObservableValue("sel", 1);
        FanIn out = new Multiplexer(4, 2);

        try {
            out.setInputs(sel, a, b, c, d);
            assertTrue(false);
        } catch (BitsException e) {
            assertTrue(true);
        }

    }

}